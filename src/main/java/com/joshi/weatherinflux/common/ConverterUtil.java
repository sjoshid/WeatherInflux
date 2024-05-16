package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import com.joshi.weatherinflux.cpuutil.CPUMetricConverter;
import org.apache.flink.api.common.serialization.DeserializationSchema;

public class ConverterUtil {
  public static <M extends Metric> DeserializationSchema<M> getConverterForMetric(
      Class<M> metricType) {
    if (metricType.equals(CPUMetric.class)) {
      return (DeserializationSchema<M>) new CPUMetricConverter();
    }
    throw new RuntimeException("Not a metric I can convert." + metricType);
  }
}
