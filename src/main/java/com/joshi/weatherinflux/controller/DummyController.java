package com.joshi.weatherinflux.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

  @GetMapping("/dummy")
  @CrossOrigin(origins = "http://localhost:3000")
  public String dummy() {
    return "Hello from Tomcat running in Spring Boot.";
  }
}
