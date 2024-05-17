package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import com.joshi.weatherinflux.cpuutil.CPUMetricConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesMetric;
import org.apache.flink.api.common.serialization.DeserializationSchema;

public class ConverterUtil {
  public static <M extends Metric> DeserializationSchema<M> getConverterForMetric(
      Class<M> metricType) {
    if (metricType.equals(CPUMetric.class)) {
      return (DeserializationSchema<M>) new CPUMetricConverter();
    } else if (metricType.equals(IntfTotalBytesMetric.class)) {
      return (DeserializationSchema<M>) new IntfTotalBytesConverter();
    }
    throw new RuntimeException("Not a metric I can convert." + metricType);
  }
}
