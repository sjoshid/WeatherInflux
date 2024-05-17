package com.joshi.weatherinflux.common;

import org.apache.flink.types.RowKind;

public interface OculusCDCRow<K> {
  RowKind getRowKind();

  K getKey();
}
