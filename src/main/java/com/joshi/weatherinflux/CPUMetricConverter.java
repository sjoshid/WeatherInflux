package com.joshi.weatherinflux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class CPUMetricConverter implements DeserializationSchema<CPUMetric> {

  @Override
  public CPUMetric deserialize(byte[] message) throws IOException {
    String og = new String(message, StandardCharsets.UTF_8);
    String[] ogTokens = og.split(",");

    return new CPUMetric(ogTokens[1], Long.parseLong(ogTokens[0]), Float.parseFloat(ogTokens[2]));
  }

  @Override
  public boolean isEndOfStream(CPUMetric nextElement) {
    return false;
  }

  @Override
  public TypeInformation<CPUMetric> getProducedType() {
    return TypeInformation.of(CPUMetric.class);
  }
}
