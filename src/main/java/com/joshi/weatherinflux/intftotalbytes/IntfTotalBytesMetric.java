package com.joshi.weatherinflux.intftotalbytes;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IntfTotalBytesMetric {
  private final long timestamp;
  private final String id;
  private final float inBytes;
  private final float outBytes;
}
