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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellIdMapper {
    private static final int GRID_SIZE = 41; // Your current grid size
    private static final int NUM_ROWS = 42000;
    private static final int CELL_SIZE = 5000;

    private final List<Long> cellIds;
    private final Map<Long, Integer> cellIdToPosition;
    private final long centerCellId;

    public CellIdMapper(List<Long> cellIds, long centerCellId) {
        this.cellIds = new ArrayList<>(cellIds);
        this.cellIdToPosition = new HashMap<>();
        this.centerCellId = centerCellId;

        // Map each cell ID to its grid position
        for (Long cellId : cellIds) {
            int gridPosition = getGridPositionFromCellId(cellId);
            if (gridPosition != -1) {
                cellIdToPosition.put(cellId, gridPosition);
            }
        }
    }

    public int getGridPositionFromCellId(long cellId) {
        // Calculate relative position from center cell
        long centerX = centerCellId / NUM_ROWS;
        long centerY = centerCellId % NUM_ROWS;

        long cellX = cellId / NUM_ROWS;
        long cellY = cellId % NUM_ROWS;

        // Calculate relative coordinates
        int relativeX = (int)((cellX - centerX) / (CELL_SIZE / 5000));
        int relativeY = (int)((cellY - centerY) / (CELL_SIZE / 5000));

        // Convert to grid position (0-based)
        int gridX = (GRID_SIZE / 2) + relativeX;
        int gridY = (GRID_SIZE / 2) + relativeY;

        // Convert to position in the grid
        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            return gridY * GRID_SIZE + gridX;
        }

        return -1; // Invalid position
    }

    public int getPosition(long cellId) {
        return cellIdToPosition.getOrDefault(cellId, -1);
    }

    public List<Long> getAllCellIds() {
        return new ArrayList<>(cellIds);
    }

    public long getCenterCellId() {
        return centerCellId;
    }
}


