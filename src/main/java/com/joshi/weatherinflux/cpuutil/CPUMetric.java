package com.joshi.weatherinflux.cpuutil;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CPUMetric {
  private final String id;
  private final long timestamp;
  private final float temp;
}
