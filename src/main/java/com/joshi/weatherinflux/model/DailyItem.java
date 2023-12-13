package com.joshi.weatherinflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DailyItem(

    @JsonProperty("moonset")
    int moonset,

    @JsonProperty("summary")
    String summary,

    @JsonProperty("sunrise")
    int sunrise,

    @JsonProperty("temp")
    Temp temp,

    @JsonProperty("moon_phase")
    int moonPhase,

    @JsonProperty("uvi")
    Object uvi,

    @JsonProperty("moonrise")
    int moonrise,

    @JsonProperty("pressure")
    int pressure,

    @JsonProperty("clouds")
    int clouds,

    @JsonProperty("feels_like")
    FeelsLike feelsLike,

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

    @JsonProperty("sunset")
    int sunset,

    @JsonProperty("weather")
    List<WeatherItem> weather,

    @JsonProperty("humidity")
    int humidity,

    @JsonProperty("wind_speed")
    Object windSpeed,

    @JsonProperty("rain")
    Object rain
) {

}