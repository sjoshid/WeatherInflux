package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.Metric;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IntfTotalBytesMetric implements Metric<String> {
  private final String id;
  private final long timestamp;
  private final float inBytes;
  private final float outBytes;

  @Override
  public String getKey() {
    return id;
  }
}
