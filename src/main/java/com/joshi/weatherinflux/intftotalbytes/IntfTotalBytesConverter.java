package com.joshi.weatherinflux.intftotalbytes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class IntfTotalBytesConverter implements DeserializationSchema<IntfTotalBytesMetric> {

  @Override
  public IntfTotalBytesMetric deserialize(byte[] message) throws IOException {
    String og = new String(message, StandardCharsets.UTF_8);
    String[] ogTokens = og.split(",");

    return new IntfTotalBytesMetric(
        ogTokens[1],
        Long.parseLong(ogTokens[0]),
        Float.parseFloat(ogTokens[2]),
        Float.parseFloat(ogTokens[3]));
  }

  @Override
  public boolean isEndOfStream(IntfTotalBytesMetric nextElement) {
    return false;
  }

  @Override
  public TypeInformation<IntfTotalBytesMetric> getProducedType() {
    return TypeInformation.of(IntfTotalBytesMetric.class);
  }
}
