package com.joshi.weatherinflux.common;

import org.apache.flink.types.Row;

public interface CDCRowConverter<K, EM extends OculusCDCRow<K>> {
  EM convert(Row r);
}
