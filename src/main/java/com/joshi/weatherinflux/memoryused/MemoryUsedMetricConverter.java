package com.joshi.weatherinflux.memoryused;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class MemoryUsedMetricConverter implements DeserializationSchema<MemoryUsedMetric> {

  @Override
  public MemoryUsedMetric deserialize(byte[] message) throws IOException {
    String og = new String(message, StandardCharsets.UTF_8);
    String[] ogTokens = og.split(",");

    return new MemoryUsedMetric(Long.parseLong(ogTokens[0]), ogTokens[1], Float.parseFloat(ogTokens[2]));
  }

  @Override
  public boolean isEndOfStream(MemoryUsedMetric nextElement) {
    return false;
  }

  @Override
  public TypeInformation<MemoryUsedMetric> getProducedType() {
    return TypeInformation.of(MemoryUsedMetric.class);
  }
}