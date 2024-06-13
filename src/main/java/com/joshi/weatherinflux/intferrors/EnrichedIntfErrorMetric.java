package com.joshi.weatherinflux.intferrors;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedIntfErrorMetric {
  private final IntfErrorMetric intfErrorMetric;
  private String deviceId;
  private String acna;
  private String sponsoredBy;
}
