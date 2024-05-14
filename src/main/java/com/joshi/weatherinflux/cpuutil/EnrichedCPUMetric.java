package com.joshi.weatherinflux.cpuutil;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedCPUMetric {
  private final CPUMetric cpuMetric;
}
