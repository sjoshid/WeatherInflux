package com.joshi.weatherinflux.intftotalbytes;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IntfTotalBytesMetric {
  private final String id;
  private final long timestamp;
  private final float inBytes;
  private final float outBytes;
}
