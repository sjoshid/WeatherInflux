package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.Metric;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.serialization.DeserializationSchema;

@Data
@RequiredArgsConstructor
public class CPUMetric implements Metric {
  private final String id;
  private final long timestamp;
  private final float temp;

  @Override
  public DeserializationSchema<Metric> getConverter() {
    return null;
  }
}
