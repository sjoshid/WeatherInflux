package com.joshi.weatherinflux.common;

import org.apache.flink.types.RowKind;

public class CPUUtilCDCRow implements OculusCDCRow<String> {
  @Override
  public RowKind getRowKind() {
    return null;
  }

  @Override
  public String getKey() {
    return null;
  }
}
