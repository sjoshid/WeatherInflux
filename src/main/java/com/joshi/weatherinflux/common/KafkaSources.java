package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class KafkaSources {
  public static KafkaSource<CPUMetric> cPUUtilKafkaSource() {
    KafkaSource<CPUMetric> source =
        KafkaSource.<CPUMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("cpu_utilization")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(ConverterUtil.getConverterForMetric(CPUMetric.class))
            .build();
    return source;
  }
}
