package com.joshi.weatherinflux.intftotalbytes;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedIntfTotalBytesMetric {
  private final IntfTotalBytesMetric intfTotalBytesMetric;
  private String deviceId;
  private Float inMaxBps;
  private Float outMaxBps;
  private Float inTotalBytes;
  private Float outTotalBytes;
}
