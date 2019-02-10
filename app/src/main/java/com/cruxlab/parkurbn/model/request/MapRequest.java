package com.cruxlab.parkurbn.model.request;

import com.cruxlab.parkurbn.model.Location;

public class MapRequest {

    public static final String DEFAULT_MEASURE = "meters";

    private Location center;
    private double radius;
    String measure = DEFAULT_MEASURE;

    public MapRequest(Location center, Double radius) {
        this.center = center;
        this.radius = radius;
    }

}
