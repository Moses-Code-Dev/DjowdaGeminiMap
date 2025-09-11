/*
 * Created by the Djowda Project Team
 * Copyright (c) 2017-2025 Djowda. All rights reserved.
 *
 * This file is part of the Djowda Project.
 *
 * Licensed under the Djowda Non-Commercial, Non-Profit License v1.0
 *
 * Permissions:
 * - You may use, modify, and share this file for non-commercial and non-profit purposes only.
 * - Commercial use of this file, in any form, requires prior written permission
 *   from the Djowda Project maintainers.
 *
 * Notes:
 * - This project is community-driven and continuously evolving.
 * - The Djowda Project reserves the right to relicense future versions.
 *
 * Last Modified: 2025-09-10 21:38
 */

package com.djowda.djowdageminimap;


import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.java.ChatFutures;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.FunctionCallPart;
import com.google.firebase.ai.type.FunctionDeclaration;
import com.google.firebase.ai.type.FunctionResponsePart;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.Schema;
import com.google.firebase.ai.type.TextPart;
import com.google.firebase.ai.type.Tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;

public class GeminiApiService {

    private static final String TAG = "GeminiApiService";
    private GenerativeModelFutures model;
    private ChatFutures chatFutures;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);

        void onError(String error);
    }

    public GeminiApiService() {
        initializeModel();
    }

    private void initializeModel() {
        // Create system instruction as Content object (role should be null for system instructions)
        Content systemInstruction = new Content(null, List.of(new TextPart(
                "You are a location coordinate provider. When a user mentions navigation, going to a place, or asks about any location, " +
                        "ALWAYS call the getLocationCoordinates function immediately without asking for confirmation. " +
                        "Never ask 'Would you like me to do that?' or similar questions. " +
                        "Examples:\n" +
                        "- 'Navigate to Eiffel Tower' -> Call getLocationCoordinates with location='Eiffel Tower'\n" +
                        "- 'Go to Tokyo' -> Call getLocationCoordinates with location='Tokyo'\n" +
                        "- 'Where is the Statue of Liberty?' -> Call getLocationCoordinates with location='Statue of Liberty'\n" +
                        "- 'Take me to Central Park' -> Call getLocationCoordinates with location='Central Park'\n" +
                        "Always extract the location name from the user's request and call the function directly."
        )));

        // Create function declaration for location lookup
        FunctionDeclaration getLocationTool = new FunctionDeclaration(
                "getLocationCoordinates",
                "Get the latitude and longitude coordinates for a specific location or place name. Use your knowledge to provide accurate coordinates.",
                Map.of(
                        "location", Schema.str("The name of the location, place, landmark, or address to get coordinates for."),
                        "latitude", Schema.numDouble("The latitude coordinate of the location"),
                        "longitude", Schema.numDouble("The longitude coordinate of the location")
                ),
                Collections.emptyList()
        );

        // Initialize the Gemini model with proper parameters
        model = GenerativeModelFutures.from(
                FirebaseAI.getInstance(GenerativeBackend.googleAI())
                        .generativeModel("gemini-2.0-flash-exp",
                                null,  // generationConfig
                                null,  // safetySettings
                                List.of(Tool.functionDeclarations(List.of(getLocationTool))), // tools
                                null,  // toolConfig
                                systemInstruction  // systemInstruction as Content type
                        )
        );

        // Start chat session
        chatFutures = model.startChat();
    }

    public void passLocationCoordinate(String userPrompt, LocationCallback callback) {
        Log.d(TAG, "Processing user prompt: " + userPrompt);

        // Send the user's question to the model
        ListenableFuture<GenerateContentResponse> response =
                chatFutures.sendMessage(new Content("user", List.of(new TextPart(userPrompt))));

        // Handle the response and check for function calls
        ListenableFuture<JsonObject> handleFunctionCallFuture = Futures.transform(response, result -> {
            Log.d(TAG, "Model response received");

            for (FunctionCallPart functionCall : result.getFunctionCalls()) {
                if (functionCall.getName().equals("getLocationCoordinates")) {
                    Log.d(TAG, "Function call detected: getLocationCoordinates");

                    Map<String, JsonElement> args = functionCall.getArgs();
                    String location = JsonElementKt.getContentOrNull(
                            JsonElementKt.getJsonPrimitive(args.get("location"))
                    );

                    Log.d(TAG, "Location requested: " + location);

                    // Extract coordinates provided by Gemini itself
                    try {
                        JsonElement latElement = args.get("latitude");
                        JsonElement lonElement = args.get("longitude");

                        if (latElement != null && lonElement != null) {
                            double lat = JsonElementKt.getDouble(JsonElementKt.getJsonPrimitive(latElement));
                            double lon = JsonElementKt.getDouble(JsonElementKt.getJsonPrimitive(lonElement));

                            Log.d(TAG, "Gemini provided coordinates - Lat: " + lat + ", Lon: " + lon);

                            // Return coordinates in expected format
                            Map<String, JsonElement> coordsMap = new HashMap<>();
                            coordsMap.put("latitude", JsonElementKt.JsonPrimitive(lat));
                            coordsMap.put("longitude", JsonElementKt.JsonPrimitive(lon));
                            coordsMap.put("location_name", JsonElementKt.JsonPrimitive(location));

                            return new JsonObject(coordsMap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error extracting coordinates from Gemini response", e);
                    }

                    // If coordinates extraction fails, log and return null
                    Log.w(TAG, "Could not extract coordinates from Gemini function call");
                    return null;
                }
            }
            return null;
        }, Executors.newSingleThreadExecutor());

        // Send function response back to model and get final response
        ListenableFuture<GenerateContentResponse> modelResponseFuture = Futures.transformAsync(
                handleFunctionCallFuture,
                functionCallResult -> {
                    if (functionCallResult != null) {
                        Log.d(TAG, "Sending function result back to model");
                        return chatFutures.sendMessage(new Content("function",
                                List.of(new FunctionResponsePart(
                                        "getLocationCoordinates", functionCallResult))));
                    } else {
                        // If no function call was made, return the original response
                        return response;
                    }
                },
                Executors.newSingleThreadExecutor()
        );

        // Handle the final response
        Futures.addCallback(modelResponseFuture, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result.getText() != null) {
                    Log.d(TAG, "Final model response: " + result.getText());

                    // Parse coordinates from the function result
                    try {
                        JsonObject coords = handleFunctionCallFuture.get();
                        if (coords != null) {
                            // Cast JsonElement to JsonPrimitive before getting double
                            JsonElement latElement = coords.get("latitude");
                            JsonElement lonElement = coords.get("longitude");

                            if (latElement != null && lonElement != null) {
                                double lat = JsonElementKt.getDouble(JsonElementKt.getJsonPrimitive(latElement));
                                double lon = JsonElementKt.getDouble(JsonElementKt.getJsonPrimitive(lonElement));

                                Log.d(TAG, "Extracted coordinates - Lat: " + lat + ", Lon: " + lon);
                                callback.onLocationReceived(lat, lon);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing coordinates", e);
                    }
                }
                callback.onError("Could not extract location coordinates from response");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error in model response", t);
                callback.onError("API call failed: " + t.getMessage());
            }
        }, Executors.newSingleThreadExecutor());
    }
}