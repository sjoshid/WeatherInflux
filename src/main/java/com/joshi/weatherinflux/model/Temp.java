package com.joshi.weatherinflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Temp(

    @JsonProperty("min")
    Object min,

    @JsonProperty("max")
    Object max,

    @JsonProperty("eve")
    Object eve,

    @JsonProperty("night")
    Object night,

    @JsonProperty("day")
    Object day,

    @JsonProperty("morn")
    Object morn
) {

}