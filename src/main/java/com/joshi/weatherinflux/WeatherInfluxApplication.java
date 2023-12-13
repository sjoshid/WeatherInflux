package com.joshi.weatherinflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeatherInfluxApplication {

  public static void main(String[] args) {
    SpringApplication.run(WeatherInfluxApplication.class, args);
  }
}
