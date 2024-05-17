package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.EnrichedMetric;
import com.joshi.weatherinflux.common.Metric;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedIntfTotalBytesMetric implements EnrichedMetric<String> {
  private final IntfTotalBytesMetric intfTotalBytesMetric;
  private Float maxBps;

  @Override
  public Metric<String> getMetric() {
    return intfTotalBytesMetric;
  }
}
