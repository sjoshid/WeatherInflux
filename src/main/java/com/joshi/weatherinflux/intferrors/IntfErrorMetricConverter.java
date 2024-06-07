package com.joshi.weatherinflux.intferrors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class IntfErrorMetricConverter implements DeserializationSchema<IntfErrorMetric> {

  @Override
  public IntfErrorMetric deserialize(byte[] message) throws IOException {
    String og = new String(message, StandardCharsets.UTF_8);
    String[] ogTokens = og.split(",");

    return new IntfErrorMetric(Long.parseLong(ogTokens[0]), ogTokens[1], Float.parseFloat(ogTokens[2]));
  }

  @Override
  public boolean isEndOfStream(IntfErrorMetric nextElement) {
    return false;
  }

  @Override
  public TypeInformation<IntfErrorMetric> getProducedType() {
    return TypeInformation.of(IntfErrorMetric.class);
  }
}
