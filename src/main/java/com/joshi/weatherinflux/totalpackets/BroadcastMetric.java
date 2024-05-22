package com.joshi.weatherinflux.totalpackets;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BroadcastMetric {
  private final String id;
  private final long timestamp;
  private final float inBytes;
  private final float outBytes;
}
