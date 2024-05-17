package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.EnrichedMetric;
import com.joshi.weatherinflux.common.Metric;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedCPUMetric implements EnrichedMetric<String> {
  private final CPUMetric cpuMetric;

  @Override
  public Metric<String> getMetric() {
    return cpuMetric;
  }
}
