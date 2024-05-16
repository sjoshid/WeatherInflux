package com.joshi.weatherinflux.common;

import org.apache.flink.api.common.serialization.DeserializationSchema;

// Maker interface
public interface Metric {
  DeserializationSchema<Metric> getConverter();
}
