package com.djowda.djowdageminimap.minmax99;



public class GeoToCellNumber {
    // Constants
    public static final int EARTH_WIDTH_METERS = 40_075_000;
    public static final int EARTH_HEIGHT_METERS = 20_000_000;
    public static final int CELL_SIZE_METERS = 500;
    public static final int NUM_ROWS = 42_000; // EARTH_HEIGHT_METERS / CELL_SIZE_METERS
    public static final int NUM_COLUMNS = 82_000; // EARTH_WIDTH_METERS / CELL_SIZE_METERS

    public static long geoToCellNumber(double latitude, double longitude) {
        // Convert to meters
        double x = (longitude + 180) * (EARTH_WIDTH_METERS / 360.0);
        double y = (EARTH_HEIGHT_METERS / 2.0) -
                Math.log(Math.tan(Math.PI / 4 + (latitude * Math.PI / 360.0))) *
                        (EARTH_HEIGHT_METERS / (2 * Math.PI));

        // Convert to cell coordinates
        int xCell = (int) Math.floor(x / CELL_SIZE_METERS);
        int yCell = (int) Math.floor(y / CELL_SIZE_METERS);

        // Calculate cell number
        return (long) xCell * NUM_ROWS + yCell;
    }
}
