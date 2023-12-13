package com.joshi.weatherinflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MinutelyItem(

    @JsonProperty("dt")
    int dt,

    @JsonProperty("precipitation")
    int precipitation
) {

}