package com.joshi.weatherinflux.intftotalbytes;

import java.time.Duration;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedIntfTotalBytesMetric {
  private final IntfTotalBytesMetric intfTotalBytesMetric;
  private Float maxBps;
}
