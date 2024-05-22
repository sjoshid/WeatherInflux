package com.joshi.weatherinflux.totalpackets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class BroadcastConverter implements DeserializationSchema<IntfPacketsMetric> {

  @Override
  public IntfPacketsMetric deserialize(byte[] message) throws IOException {
    String og = new String(message, StandardCharsets.UTF_8);
    String[] ogTokens = og.split(",");

    IntfPacketsMetric m =
        new IntfPacketsMetric(
            ogTokens[1],
            Long.parseLong(ogTokens[0]),
            Float.parseFloat(ogTokens[2]),
            Float.parseFloat(ogTokens[3]),
            MetricType.BROADCAST);

    return m;
  }

  @Override
  public boolean isEndOfStream(IntfPacketsMetric nextElement) {
    return false;
  }

  @Override
  public TypeInformation<IntfPacketsMetric> getProducedType() {
    return TypeInformation.of(IntfPacketsMetric.class);
  }
}
