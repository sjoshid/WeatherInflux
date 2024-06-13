package com.joshi.weatherinflux.intferrors;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IntfErrorMetric {
  private final long timestamp;
  private final String id;
  private final float inErrors;
  private final float outErrors;
}
