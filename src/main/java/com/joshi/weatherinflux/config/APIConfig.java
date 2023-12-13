package com.joshi.weatherinflux.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class APIConfig {

  @Value("${openWeatherAPIKey}")
  private String owAPIKey;

  @Value("${openWeatherBaseAPIUrl}")
  private String owBaseAPIUrl;

  @Value("${influxToken}")
  private String influxToken;

  @Bean
  public UriBuilder openWeatherUriBuilder() {
    var urib = UriComponentsBuilder.fromHttpUrl(owBaseAPIUrl).query("lat={lat}&lon={lon}")
        .queryParam("appid", owAPIKey);
    return urib;
  }

  @Bean
  public InfluxDBClient influxDBClient() {
    return InfluxDBClientFactory.create("http://localhost:8086", influxToken.toCharArray(), "ca096bb5f42bb6e0", "weather");
  }
}
