package com.joshi.weatherinflux.intfdiscards;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class IntfDiscardMetricConverter implements DeserializationSchema<IntfDiscardMetric> {

  @Override
  public IntfDiscardMetric deserialize(byte[] message) throws IOException {
    String og = new String(message, StandardCharsets.UTF_8);
    String[] ogTokens = og.split(",");

    return new IntfDiscardMetric(Long.parseLong(ogTokens[0]), ogTokens[1], Float.parseFloat(ogTokens[2]));
  }

  @Override
  public boolean isEndOfStream(IntfDiscardMetric nextElement) {
    return false;
  }

  @Override
  public TypeInformation<IntfDiscardMetric> getProducedType() {
    return TypeInformation.of(IntfDiscardMetric.class);
  }
}
