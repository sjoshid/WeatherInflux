package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.OculusCDCRow;
import org.apache.flink.types.RowKind;

public class IntfTotalBytesCDCRow implements OculusCDCRow<String> {
  @Override
  public RowKind getRowKind() {
    return null;
  }

  @Override
  public String getKey() {
    return null;
  }
}
