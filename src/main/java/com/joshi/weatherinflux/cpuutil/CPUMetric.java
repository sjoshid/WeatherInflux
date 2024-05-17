package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.Metric;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CPUMetric implements Metric<String> {
  private final String id;
  private final long timestamp;
  private final float temp;

  @Override
  public String getKey() {
    return null;
  }
}
