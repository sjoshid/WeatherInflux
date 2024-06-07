package com.joshi.weatherinflux.memoryused;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedMemoryUsedMetric {
  private final MemoryUsedMetric memoryUsedMetric;
  private String acna;
  private String sponsoredBy;
  private String country;
}
