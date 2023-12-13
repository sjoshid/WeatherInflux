package com.joshi.weatherinflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherItem(

    @JsonProperty("icon")
    String icon,

    @JsonProperty("description")
    String description,

    @JsonProperty("main")
    String main,

    @JsonProperty("id")
    int id
) {

}