package com.joshi.weatherinflux.memoryused;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MemoryUsedMetric {
  private final long timestamp;
  private final String deviceId;
  private final float memoryUsed;
}
