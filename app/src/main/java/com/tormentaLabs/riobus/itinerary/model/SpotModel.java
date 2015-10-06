package com.tormentaLabs.riobus.itinerary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by limazix on 05/10/15.
 */
public class SpotModel {

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("returning")
    private String returning;

    public SpotModel() {
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getReturning() {
        return returning;
    }

    public void setReturning(String returning) {
        this.returning = returning;
    }
}
