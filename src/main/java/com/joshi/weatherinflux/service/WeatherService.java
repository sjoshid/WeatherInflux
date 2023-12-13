package com.joshi.weatherinflux.service;

import com.influxdb.client.InfluxDBClient;
import com.joshi.weatherinflux.model.MegaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Service
@RequiredArgsConstructor
public class WeatherService {

  private final UriBuilder openWeatherUriBuilder;

  private final InfluxDBClient influxDBClient;

  @Scheduled(cron = "10,20,30,40,50 * * * * *")
  public void getCurrentWeather() {
    // lat=33.44&lon=-94.04&appid={{API Key}}
    RestClient rs = RestClient.create();
    ResponseEntity<MegaResponse> entity = rs.get()
        // this is Chicago.
        .uri(openWeatherUriBuilder.build("33.44", "-94.04"))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(MegaResponse.class);

    if (entity.getStatusCode().is2xxSuccessful()) {
      final var mr = entity.getBody();
      if (mr != null) {
        var writeApi = influxDBClient.getWriteApiBlocking();
        var p = mr.point();
        writeApi.writePoint(p);
      } else {
        System.out.println("API call was success but deserialization failed.");
      }
    } else {
      // Some error
      System.err.println("error is " + entity.getStatusCode());
    }
  }
}
