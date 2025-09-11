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
 * Last Modified: 2025-09-10 22:30
 */

package com.djowda.djowdageminimap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.djowda.djowdageminimap.MapTest.Component;
import com.djowda.djowdageminimap.MapTest.Custom2DScrollView;
import com.djowda.djowdageminimap.MapTest.GridAdapter;
import com.djowda.djowdageminimap.MapTest.TileMap;
import com.djowda.djowdageminimap.minmax99.NearbyComponentFetcher;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MapFragment extends Fragment implements GridAdapter.ItemClickListener {
    private static final String TAG = "MapFragment";

    // UI Components
    private RecyclerView recyclerView;
    private Custom2DScrollView scrollView;
    private GridAdapter adapter;
    private final android.os.Handler updateHandler = new Handler(Looper.getMainLooper());

    // Statistics TextViews
    private TextView tvUsers, tvDeliveryMen, tvStores, tvTransport, tvRestaurants;
    private TextView tvFactory, tvWholeSellers, tvFarmers, tvSeedProviders;

    // Chat UI Components
    private EditText etAiChat;
    private Button btnSendMessage;

    // Statistics counters
    private int[] stats = new int[9]; // 0: Users, 1: DeliveryMen, 2: Stores, etc.

    // Services
    private GeminiApiService geminiApiService;
    private NavigationService navigationService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services
        initializeServices();

        // Initialize UI components
        initializeViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Load initial data
        navigateToCell(644966003L, 20);

        // Generate initial random components
        generateRandomComponents();
    }

    private void initializeServices() {
        geminiApiService = new GeminiApiService();
        navigationService = new NavigationService();
        Log.d(TAG, "Services initialized");
    }

    private void initializeViews(View view) {
        // Map components
        recyclerView = view.findViewById(R.id.gridRecyclerView);
        scrollView = view.findViewById(R.id.customScrollView);

        // Statistics TextViews
        tvUsers = view.findViewById(R.id.tvUsers);
        tvDeliveryMen = view.findViewById(R.id.tvDeliveryMen);
        tvStores = view.findViewById(R.id.tvStores);
        tvTransport = view.findViewById(R.id.tvTransport);
        tvRestaurants = view.findViewById(R.id.tvRestaurants);
        tvFactory = view.findViewById(R.id.tvFactory);
        tvWholeSellers = view.findViewById(R.id.tvWholeSellers);
        tvFarmers = view.findViewById(R.id.tvFarmers);
        tvSeedProviders = view.findViewById(R.id.tvSeedProviders);

        // Chat components
        etAiChat = view.findViewById(R.id.etAiChat);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        // Set click listener for send button
        btnSendMessage.setOnClickListener(this::onSendMessageClicked);
    }

    // In MapFragment.java, modify the onSendMessageClicked method:

    private void onSendMessageClicked(View view) {
        String userPrompt = etAiChat.getText().toString().trim();

        if (userPrompt.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "User prompt: " + userPrompt);

        // Capture start time for response measurement
        long startTime = System.currentTimeMillis();

        // Disable button to prevent multiple requests
        btnSendMessage.setEnabled(false);
        btnSendMessage.setText("Processing...");

        // Clear the input field
        etAiChat.setText("");

        // Call Gemini API to get coordinates
        geminiApiService.passLocationCoordinate(userPrompt, new GeminiApiService.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                updateHandler.post(() -> {
                    Log.d(TAG, "Received coordinates - Lat: " + latitude + ", Lon: " + longitude);

                    // Convert coordinates to cell ID and navigate
                    navigationService.geoToCellNumber(latitude, longitude, new NavigationService.NavigationCallback() {
                        @Override
                        public void onNavigationComplete(long cellId) {
                            updateHandler.post(() -> {
                                // Calculate response time
                                long responseTime = System.currentTimeMillis() - startTime;

                                Log.d(TAG, "Navigation complete to cell ID: " + cellId);
                                navigateToCell(cellId, 20);

                                String locationDesc = navigationService.getLocationDescription(latitude, longitude);

                                // Display response time in EditText
                                etAiChat.setHint("AI Response: " + responseTime + "ms - " + locationDesc);

                                Toast.makeText(requireContext(),
                                        "Navigated to " + locationDesc + " (Cell: " + cellId + ")",
                                        Toast.LENGTH_LONG).show();

                                resetSendButton();
                            });
                        }

                        @Override
                        public void onNavigationError(String error) {
                            updateHandler.post(() -> {
                                // Calculate response time
                                long responseTime = System.currentTimeMillis() - startTime;

                                Log.e(TAG, "Navigation error: " + error);

                                // Display error with response time in EditText
                                etAiChat.setHint("AI Error (" + responseTime + "ms): " + error);

                                Toast.makeText(requireContext(), "Navigation error: " + error, Toast.LENGTH_SHORT).show();
                                resetSendButton();
                            });
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                updateHandler.post(() -> {
                    // Calculate response time
                    long responseTime = System.currentTimeMillis() - startTime;

                    Log.e(TAG, "API error: " + error);

                    // Display error with response time in EditText
                    etAiChat.setHint("AI Error (" + responseTime + "ms): " + error);

                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    resetSendButton();
                });
            }
        });

        // For now, also generate random components for testing
        generateRandomComponents();
    }

    private void resetSendButton() {
        btnSendMessage.setEnabled(true);
        btnSendMessage.setText("Send");
    }

    private void generateRandomComponents() {
        Random random = new Random();

        // Reset stats
        Arrays.fill(stats, 0);

        // Simulate counts for each type of component
        stats[0] = random.nextInt(5000); // Users
        stats[1] = random.nextInt(1000); // DeliveryMen
        stats[2] = random.nextInt(2000); // Stores
        stats[3] = random.nextInt(500);  // Transport
        stats[4] = random.nextInt(1500); // Restaurants
        stats[5] = random.nextInt(300);  // Factory
        stats[6] = random.nextInt(700);  // WholeSellers
        stats[7] = random.nextInt(2500); // Farmers
        stats[8] = random.nextInt(400);  // SeedProviders

        // Update UI
        tvUsers.setText("Users: " + stats[0]);
        tvDeliveryMen.setText("DeliveryMen: " + stats[1]);
        tvStores.setText("Stores: " + stats[2]);
        tvTransport.setText("Transport: " + stats[3]);
        tvRestaurants.setText("Restaurants: " + stats[4]);
        tvFactory.setText("Factories: " + stats[5]);
        tvWholeSellers.setText("WholeSellers: " + stats[6]);
        tvFarmers.setText("Farmers: " + stats[7]);
        tvSeedProviders.setText("SeedProviders: " + stats[8]);
    }

    private void navigateToCell(long centerCellId, int range) {
        Log.d(TAG, "Navigating to cell: " + centerCellId + " with range: " + range);

        // Clear existing data before loading new data
        adapter.clearAllData();

        fetchNearbyComponentsByCellId(centerCellId, range);
    }

    private void fetchNearbyComponentsByCellId(long cellId, int range) {
        // Testing the data fetching dummy mode
        NearbyComponentFetcher fetcher = new NearbyComponentFetcher(null);
        fetcher.fetchNearbyComponentsDummy(cellId, range, new NearbyComponentFetcher.OnFetchComplete() {
            @Override
            public void onResult(NearbyComponentFetcher.FetchResult result) {
                updateHandler.post(() -> {
                    // Process each cell in the result
                    for (Long cellId : result.mapper.getAllCellIds()) {
                        int gridPosition = result.mapper.getGridPositionFromCellId(cellId);
                        if (gridPosition != -1) {
                            List<Component> components = result.cellComponents.get(cellId);
                            if (components != null && !components.isEmpty()) {
                                // Update the grid with the first component (or handle multiple as needed)
                                adapter.updateCell(gridPosition, cellId, components.get(0));
                            }
                        }
                    }
                });

                // Center the grid
                scrollView.post(() -> scrollView.centerOnGrid());
            }

            @Override
            public void onError(Exception e) {
                updateHandler.post(() -> {
                    Toast.makeText(requireContext(), "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), TileMap.getMapSize());
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new GridAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        adapter.setClickListener(this);

        // Center the grid
        scrollView.post(() -> scrollView.centerOnGrid());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onItemClick(View view, int position, long cellId) {
        if (cellId != -1) {
            Toast.makeText(requireContext(), "Cell ID: " + cellId, Toast.LENGTH_SHORT).show();
            // Option: Navigate to the clicked cell
            // navigateToCell(cellId, 20);
        } else {
            Toast.makeText(requireContext(), "Empty cell at position: " + position, Toast.LENGTH_SHORT).show();
        }
    }
}
