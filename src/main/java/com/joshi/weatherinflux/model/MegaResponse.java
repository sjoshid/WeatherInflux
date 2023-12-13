package com.joshi.weatherinflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import java.time.Instant;
import java.util.List;

public record MegaResponse(
    @JsonProperty("current")
    Current current,

    @JsonProperty("timezone")
    String timezone,

    @JsonProperty("timezone_offset")
    int timezoneOffset,

    @JsonProperty("daily")
    List<DailyItem> daily,

    @JsonProperty("lon")
    Float lon,

    @JsonProperty("hourly")
    List<HourlyItem> hourly,

    @JsonProperty("minutely")
    List<MinutelyItem> minutely,

    @JsonProperty("lat")
    Float lat
) {

  public Point point() {
    var point = Point.measurement("weather")
        .addTag("city", timezone())
        .addField("temp", current().temp())
        .time(current().dt(), WritePrecision.S);
    return point;
  }
}