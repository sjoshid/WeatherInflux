package com.joshi.weatherinflux.intfdiscards;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedIntfDiscardMetric {
  private final IntfDiscardMetric intfDiscardMetric;
  private String deviceId;
}
