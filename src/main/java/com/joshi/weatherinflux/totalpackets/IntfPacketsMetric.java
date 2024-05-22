package com.joshi.weatherinflux.totalpackets;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IntfPacketsMetric {
  private final String id;
  private final long timestamp;
  private final float inPackets;
  private final float outPackets;
  private final MetricType type;
}
