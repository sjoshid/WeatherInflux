package com.joshi.weatherinflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record HourlyItem(

    @JsonProperty("temp")
    Object temp,

    @JsonProperty("visibility")
    int visibility,

    @JsonProperty("uvi")
    Object uvi,

    @JsonProperty("pressure")
    int pressure,

    @JsonProperty("clouds")
    int clouds,

    @JsonProperty("feels_like")
    Object feelsLike,

    @JsonProperty("wind_gust")
    Object windGust,

    @JsonProperty("dt")
    int dt,

    @JsonProperty("pop")
    int pop,

    @JsonProperty("wind_deg")
    int windDeg,

    @JsonProperty("dew_point")
    Object dewPoint,

    @JsonProperty("weather")
    List<WeatherItem> weather,

    @JsonProperty("humidity")
    int humidity,

    @JsonProperty("wind_speed")
    Object windSpeed
) {

}