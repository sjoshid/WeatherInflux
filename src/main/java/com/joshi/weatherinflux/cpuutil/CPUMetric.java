package com.joshi.weatherinflux.cpuutil;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CPUMetric {
  private final long timestamp;
  private final String deviceId;
  private final float util;
}
