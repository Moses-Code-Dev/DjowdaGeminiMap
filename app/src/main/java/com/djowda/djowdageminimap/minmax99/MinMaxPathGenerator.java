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


import java.util.Locale;

public class MinMaxPathGenerator {

    /**
     * Constructs a database path based on the minMax99 structure.
     * @param cellNumber The cell number as a string.
     * @return The constructed database path.
     * @throws IllegalArgumentException if the cell number is invalid.
     */
    public static String constructDbPath(String cellNumber) {
        try {
            // Parse cell number to long
            long cell = Long.parseLong(cellNumber);

            // Divide the cell number into hierarchical levels
            long lvl1 = cell / (1000 * 1000); // Top-level node
            long lvl2 = (cell / 1000) % 1000; // Intermediate node
            long lvl3 = cell % 1000; // Leaf node

            // Construct path
            return String.format(Locale.ROOT, "%d/%d/%d",
                    lvl1, lvl2, lvl3);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cell number format: " + cellNumber, e);
        }
    }

}



