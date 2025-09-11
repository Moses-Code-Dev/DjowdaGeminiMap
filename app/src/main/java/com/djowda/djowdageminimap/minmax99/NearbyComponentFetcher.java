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

package com.djowda.djowdageminimap.minmax99;


import com.djowda.djowdageminimap.MapTest.CellIdMapper;
import com.djowda.djowdageminimap.MapTest.Component;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NearbyComponentFetcher {
    private final DatabaseReference dbRef;

    public NearbyComponentFetcher(DatabaseReference dbRef) {
        this.dbRef = dbRef;
    }

    public static class FetchResult {
        public final CellIdMapper mapper;
        public final Map<Long, List<Component>> cellComponents;

        public FetchResult(CellIdMapper mapper, Map<Long, List<Component>> cellComponents) {
            this.mapper = mapper;
            this.cellComponents = cellComponents;
        }
    }

    /**
     * Dummy mode — no Firebase call, random components.
     */
    public void fetchNearbyComponentsDummy(long centerCellId, int range, OnFetchComplete callback) {
        List<Long> nearbyIds = NearbyCellUtils.getNearbyCellIds(centerCellId, range);
        CellIdMapper mapper = new CellIdMapper(nearbyIds, centerCellId); // Pass center cell ID
        Map<Long, List<Component>> cellComponents = new HashMap<>();
        Random random = new Random();

        for (Long cellId : nearbyIds) {
            if (random.nextFloat() < 0.3f) { // 30% chance of having a component
                List<Component> components = new ArrayList<>();
                boolean isOpen = random.nextBoolean(); // Random open/closed state
                String storeName = "Store " + (cellId % 1000); // Generate unique store names
                components.add(new Component("dummyId_" + cellId, storeName, isOpen, cellId));
                cellComponents.put(cellId, components);
            }
        }

        callback.onResult(new FetchResult(mapper, cellComponents));
    }

    /**
     * Real Firebase mode.
     */
    public void fetchNearbyComponentsFirebase(long centerCellId, int range, OnFetchComplete callback) {
        List<Long> nearbyIds = NearbyCellUtils.getNearbyCellIds(centerCellId, range);
        CellIdMapper mapper = new CellIdMapper(nearbyIds, centerCellId); // Pass center cell ID
        Map<Long, List<Component>> cellComponents = new HashMap<>();
        AtomicInteger pending = new AtomicInteger(nearbyIds.size());

        for (Long cellId : nearbyIds) {
            String path = MinMaxPathGenerator.constructDbPath(String.valueOf(cellId));
            DatabaseReference cellRef = dbRef.child("test").child(path).child("s");

            cellRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    List<Component> components = new ArrayList<>();

                    for (DataSnapshot compSnap : task.getResult().getChildren()) {
                        String compId = compSnap.getKey();
                        String cn = compSnap.child("cn").getValue(String.class);
                        String o = compSnap.child("o").getValue(String.class);
                        boolean isOpen = "1".equals(o);

                        components.add(new Component(compId, cn, isOpen, cellId));
                    }

                    if (!components.isEmpty()) {
                        cellComponents.put(cellId, components);
                    }
                }

                if (pending.decrementAndGet() == 0) {
                    callback.onResult(new FetchResult(mapper, cellComponents));
                }
            }).addOnFailureListener(e -> {
                if (pending.decrementAndGet() == 0) {
                    callback.onError(e);
                }
            });
        }
    }

    public interface OnFetchComplete {
        void onResult(FetchResult result);
        void onError(Exception e);
    }
}


//public class NearbyComponentFetcher {
//
//    private final DatabaseReference dbRef;
//
//    public NearbyComponentFetcher(DatabaseReference dbRef) {
//        this.dbRef = dbRef;
//    }
//
//    /**
//     * Fetches nearby cells and their components (dummy mode for now).
//     */
//    public List<CellData> fetchNearbyComponentsDummy(long centerCellId, int range) {
//        List<Long> nearbyIds = NearbyCellUtils.getNearbyCellIds(centerCellId, range);
//        List<CellData> cells = new ArrayList<>();
//        Random random = new Random();
//
//        for (Long id : nearbyIds) {
//            String path = MinMaxPathGenerator.constructDbPath(String.valueOf(id));
//
//            CellData cellData = new CellData();
//            cellData.cellId = id;
//            cellData.path = path;
//            cellData.hasComponent = random.nextFloat() < 0.3f; // 30% chance to have a component
//            cellData.components = new ArrayList<>();
//
//            // Dummy component if exists
//            if (cellData.hasComponent) {
//                cellData.components.add(new Component("dummyId", "Dummy Store", "0"));
//            }
//
//            cells.add(cellData);
//        }
//
//        return cells;
//    }
//
//    /**
//     * Fetches nearby cells and their components from Firebase (real mode).
//     */
//    public void fetchNearbyComponentsFirebase(long centerCellId, int range, OnFetchComplete callback) {
//        List<Long> nearbyIds = NearbyCellUtils.getNearbyCellIds(centerCellId, range);
//        List<CellData> cells = new ArrayList<>();
//
//        AtomicInteger pending = new AtomicInteger(nearbyIds.size());
//
//        for (Long id : nearbyIds) {
//            String path = MinMaxPathGenerator.constructDbPath(String.valueOf(id));
//            DatabaseReference cellRef = dbRef.child("test").child(path).child("s");
//
//            CellData cellData = new CellData();
//            cellData.cellId = id;
//            cellData.path = path;
//            cellData.components = new ArrayList<>();
//
//            cellRef.get().addOnCompleteListener(task -> {
//                if (task.isSuccessful() && task.getResult().exists()) {
//                    cellData.hasComponent = true;
//                    for (DataSnapshot compSnap : task.getResult().getChildren()) {
//                        String compId = compSnap.getKey();
//                        String cn = compSnap.child("cn").getValue(String.class);
//                        String o = compSnap.child("o").getValue(String.class);
//                        cellData.components.add(new Component(compId, cn, o));
//                    }
//                } else {
//                    cellData.hasComponent = false;
//                }
//
//                synchronized (cells) {
//                    cells.add(cellData);
//                }
//
//                if (pending.decrementAndGet() == 0) {
//                    callback.onResult(cells);
//                }
//            }).addOnFailureListener(e -> {
//                if (pending.decrementAndGet() == 0) {
//                    callback.onError(e);
//                }
//            });
//        }
//    }
//
//    public interface OnFetchComplete {
//        void onResult(List<CellData> components);
//        void onError(Exception e);
//    }
//}
