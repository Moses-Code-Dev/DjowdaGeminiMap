/*
 *
 *  * Created by the Djowda Project Team
 *  * Copyright (c) 2017-2025 Djowda. All rights reserved.
 *  *
 *  * This file is part of the Djowda Project.
 *  *
 *  * Licensed under the Djowda Non-Commercial, Non-Profit License v1.0
 *  *
 *  * Permissions:
 *  * - You may use, modify, and share this file for non-commercial and non-profit purposes only.
 *  * - Commercial use of this file, in any form, requires prior written permission
 *  *   from the Djowda Project maintainers.
 *  *
 *  * Notes:
 *  * - This project is community-driven and continuously evolving.
 *  * - The Djowda Project reserves the right to relicense future versions.
 *  *
 *  * Last Modified: 2025-08-16 18:01
 *
 */

package com.djowda.djowdageminimap.MapTest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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


import com.djowda.djowdageminimap.R;
import com.djowda.djowdageminimap.minmax99.NearbyComponentFetcher;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MapFragment extends Fragment implements GridAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private Custom2DScrollView scrollView;
    private GridAdapter adapter;
    private final Handler updateHandler = new Handler(Looper.getMainLooper());

    // Statistics TextViews
    private TextView tvUsers, tvDeliveryMen, tvStores, tvTransport, tvRestaurants;
    private TextView tvFactory, tvWholeSellers, tvFarmers, tvSeedProviders;

//    private EditText etCellId;
//    private Button btnNavigate;

    private EditText etAiChat;
    private Button btnSendMessage;

    // Statistics counters
    private int[] stats = new int[9]; // 0: Users, 1: DeliveryMen, 2: Stores, etc.

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.gridRecyclerView);
        scrollView = view.findViewById(R.id.customScrollView);

        tvUsers = view.findViewById(R.id.tvUsers);
        tvDeliveryMen = view.findViewById(R.id.tvDeliveryMen);
        tvStores = view.findViewById(R.id.tvStores);
        tvTransport = view.findViewById(R.id.tvTransport);
        tvRestaurants = view.findViewById(R.id.tvRestaurants);
        tvFactory = view.findViewById(R.id.tvFactory);
        tvWholeSellers = view.findViewById(R.id.tvWholeSellers);
        tvFarmers = view.findViewById(R.id.tvFarmers);
        tvSeedProviders = view.findViewById(R.id.tvSeedProviders);

//        etCellId = view.findViewById(R.id.etCellId);
//        btnNavigate = view.findViewById(R.id.btnNavigate);

        etAiChat = view.findViewById(R.id.etAiChat);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);


//        btnNavigate.setOnClickListener(view1 -> {
//            String cellIdText = etCellId.getText().toString().trim();
//            if (!cellIdText.isEmpty()) {
//                try {
//                    long cellId = Long.parseLong(cellIdText);
//                    navigateToCell(cellId, 20);
//                } catch (NumberFormatException e) {
//                    Toast.makeText(requireContext(), "Invalid cell ID format", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//            generateRandomComponents();
//        });


        btnSendMessage.setOnClickListener(view1 -> {

            // TODO: 10/09/2025 >> send prompt to Ai and use the respond to call the navigateToCell
            //  with "GeoToCellNumber" as parameter (the cellId is the returned value)
            //  the Ai respond should be a real coordinate
            //  flow example: navigate to Eiffel Tower >> Ai respond should be lan and lat >> we gonna use this
            //  lan and lat for function calling

            // for now we gonna start by a simple test of function calling to test the Api request by just printing the
            // the respond in the console log


            generateRandomComponents();
        });

        setupRecyclerView();

        // Load initial data
        navigateToCell(644966003L, 20);

        generateRandomComponents();
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
        // Clear existing data before loading new data
        adapter.clearAllData();

        // Update the current cell ID display
//        etCellId.setText(String.valueOf(centerCellId));

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
                Toast.makeText(requireContext(), "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            // Option 1: Just show the cell ID
            Toast.makeText(requireContext(), "Cell ID: " + cellId, Toast.LENGTH_SHORT).show();

            // Option 2: Navigate to the clicked cell (uncomment if desired)
            // navigateToCell(cellId, 20);
        } else {
            Toast.makeText(requireContext(), "Empty cell at position: " + position, Toast.LENGTH_SHORT).show();
        }
    }


}





