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

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NearbyCellUtils {

    private static final int TOTAL_COLUMNS = 82000;
    private static final int TOTAL_ROWS = 42000;

    /**
     * Get all nearby cell IDs in a square range around a given cell.
     * @param currentCellId The center cell ID.
     * @param range Number of layers around the cell (1 = 3x3 grid).
     * @return List of nearby cell IDs including the center.
     */
    public static List<Long> getNearbyCellIds(long currentCellId, int range) {
        List<Long> nearbyCells = new ArrayList<>();

        // Convert cell ID to (col, row)
        int col = (int) ((currentCellId - 1) / TOTAL_ROWS);
        int row = (int) ((currentCellId - 1) % TOTAL_ROWS);

        for (int c = col - range; c <= col + range; c++) {
            for (int r = row - range; r <= row + range; r++) {
                if (c >= 0 && c < TOTAL_COLUMNS && r >= 0 && r < TOTAL_ROWS) {
                    long cellId = (long) c * TOTAL_ROWS + r + 1;
                    nearbyCells.add(cellId);
                }
            }
        }

        return nearbyCells;
    }

    // Test function
    public static void testNearbyCells() {
        long testCellId = 644966003L; // example
        int range = 3;
        List<Long> results = getNearbyCellIds(testCellId, range);
        for (Long id : results) {
            Log.d("NearbyCellUtils", "Cell: " + id);
        }
    }
}
