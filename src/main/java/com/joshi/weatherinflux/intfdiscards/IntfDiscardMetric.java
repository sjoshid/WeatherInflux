package com.joshi.weatherinflux.intfdiscards;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IntfDiscardMetric {
  private final long timestamp;
  private final String id;
  private final float temp;
}
