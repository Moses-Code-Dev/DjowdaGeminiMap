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
 * Last Modified: 2025-09-10 22:26
 */

package com.djowda.djowdageminimap;

import android.util.Log;

import com.djowda.djowdageminimap.minmax99.GeoToCellNumber;

public class NavigationService {
        private static final String TAG = "NavigationService";

        public interface NavigationCallback {
            void onNavigationComplete(long cellId);
            void onNavigationError(String error);
        }

        /**
         * Convert geographic coordinates (latitude, longitude) to cell ID using real algorithm
         */
        public void geoToCellNumber(double latitude, double longitude, NavigationCallback callback) {
            Log.d(TAG, "Converting coordinates to cell ID - Lat: " + latitude + ", Lon: " + longitude);

            try {
                // Validate coordinates first
                if (!isValidCoordinates(latitude, longitude)) {
                    callback.onNavigationError("Invalid coordinates: Latitude must be between -90 and 90, Longitude between -180 and 180");
                    return;
                }

                // Use the real GeoToCellNumber conversion algorithm
                long cellId = GeoToCellNumber.geoToCellNumber(latitude, longitude);

                Log.d(TAG, "Converted to cell ID: " + cellId + " using real algorithm");
                callback.onNavigationComplete(cellId);

            } catch (Exception e) {
                Log.e(TAG, "Error converting coordinates to cell ID", e);
                callback.onNavigationError("Failed to convert coordinates to cell ID: " + e.getMessage());
            }
        }

        /**
         * Navigate to a specific cell ID with a given range
         */
        public void navigateToCell(long cellId, int range, NavigationCallback callback) {
            Log.d(TAG, "Navigating to cell ID: " + cellId + " with range: " + range);

            try {
                // Validate inputs
                if (cellId < 0) {
                    callback.onNavigationError("Invalid cell ID: must be non-negative");
                    return;
                }

                if (range <= 0) {
                    callback.onNavigationError("Invalid range: must be positive");
                    return;
                }

                // Validate cell ID is within valid bounds
                long maxCellId = (long) GeoToCellNumber.NUM_COLUMNS * GeoToCellNumber.NUM_ROWS - 1;
                if (cellId > maxCellId) {
                    callback.onNavigationError("Invalid cell ID: exceeds maximum cell ID of " + maxCellId);
                    return;
                }

                Log.d(TAG, "Navigation to cell " + cellId + " validated successfully");
                callback.onNavigationComplete(cellId);

            } catch (Exception e) {
                Log.e(TAG, "Error navigating to cell", e);
                callback.onNavigationError("Navigation failed: " + e.getMessage());
            }
        }



        /**
         * Convert cell ID back to approximate geographic coordinates (reverse conversion)
         */
        // FIXME: 11/09/2025 not tested yet

        public void cellToGeo(long cellId, NavigationCallback callback) {
            Log.d(TAG, "Converting cell ID to coordinates: " + cellId);

            try {
                // Validate cell ID
                long maxCellId = (long) GeoToCellNumber.NUM_COLUMNS * GeoToCellNumber.NUM_ROWS - 1;
                if (cellId < 0 || cellId > maxCellId) {
                    callback.onNavigationError("Invalid cell ID: " + cellId);
                    return;
                }

                // Reverse the cell calculation to get approximate coordinates
                int xCell = (int) (cellId / GeoToCellNumber.NUM_ROWS);
                int yCell = (int) (cellId % GeoToCellNumber.NUM_ROWS);

                // Convert cell coordinates back to meters
                double x = xCell * GeoToCellNumber.CELL_SIZE_METERS;
                double y = yCell * GeoToCellNumber.CELL_SIZE_METERS;

                // Convert meters back to geographic coordinates (approximate)
                double longitude = (x / (GeoToCellNumber.EARTH_WIDTH_METERS / 360.0)) - 180;

                // Reverse Mercator projection for latitude (approximate)
                double mercatorY = (GeoToCellNumber.EARTH_HEIGHT_METERS / 2.0) - y;
                double latitude = (2 * Math.atan(Math.exp(mercatorY * (2 * Math.PI) / GeoToCellNumber.EARTH_HEIGHT_METERS)) - Math.PI / 2) * 180 / Math.PI;

                Log.d(TAG, "Cell " + cellId + " converted to approximate coordinates: " + latitude + ", " + longitude);
                callback.onNavigationComplete(cellId); // Still return the cell ID as this is for validation

            } catch (Exception e) {
                Log.e(TAG, "Error converting cell ID to coordinates", e);
                callback.onNavigationError("Failed to convert cell ID to coordinates: " + e.getMessage());
            }
        }

        /**
         * Get a human-readable description of coordinates
         */
        // FIXME: 11/09/2025 not tested yet

        public String getLocationDescription(double latitude, double longitude) {
            try {
                long cellId = GeoToCellNumber.geoToCellNumber(latitude, longitude);
                return String.format("Coordinates: %.6f, %.6f (Cell ID: %d)", latitude, longitude, cellId);
            } catch (Exception e) {
                return String.format("Coordinates: %.6f, %.6f (Cell conversion failed)", latitude, longitude);
            }
        }

        /**
         * Validate if coordinates are within valid ranges
         */
        // FIXME: 11/09/2025 not tested yet
        public boolean isValidCoordinates(double latitude, double longitude) {
            return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        }

        /**
         * Get cell information for debugging
         */
        // FIXME: 11/09/2025 not tested yet
        public String getCellInfo(long cellId) {
            try {
                int xCell = (int) (cellId / GeoToCellNumber.NUM_ROWS);
                int yCell = (int) (cellId % GeoToCellNumber.NUM_ROWS);

                return String.format("Cell ID: %d, Grid Position: [%d, %d], Cell Size: %dm",
                        cellId, xCell, yCell, GeoToCellNumber.CELL_SIZE_METERS);
            } catch (Exception e) {
                return "Cell info unavailable: " + e.getMessage();
            }
        }

        /**
         * Calculate distance between two cell IDs (approximate)
         */

        // FIXME: 11/09/2025 not tested yet
        public double getDistanceBetweenCells(long cellId1, long cellId2) {
            try {
                // Get cell grid positions
                int x1 = (int) (cellId1 / GeoToCellNumber.NUM_ROWS);
                int y1 = (int) (cellId1 % GeoToCellNumber.NUM_ROWS);
                int x2 = (int) (cellId2 / GeoToCellNumber.NUM_ROWS);
                int y2 = (int) (cellId2 % GeoToCellNumber.NUM_ROWS);

                // Calculate distance in cells, then convert to meters
                double cellDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                return cellDistance * GeoToCellNumber.CELL_SIZE_METERS;

            } catch (Exception e) {
                Log.e(TAG, "Error calculating distance between cells", e);
                return -1; // Return -1 to indicate error
            }
        }
    }
