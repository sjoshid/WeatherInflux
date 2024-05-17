package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesMetric;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class KafkaSources {
  public static KafkaSource<CPUMetric> cpuMetricKafkaSource =
      KafkaSource.<CPUMetric>builder()
          .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
          .setTopics("cpu_utilization")
          .setGroupId("my-group")
          .setStartingOffsets(OffsetsInitializer.latest())
          .setValueOnlyDeserializer(ConverterUtil.getConverterForMetric(CPUMetric.class))
          .build();

  public static KafkaSource<IntfTotalBytesMetric> intfTotalBytesMetricKafkaSource =
      KafkaSource.<IntfTotalBytesMetric>builder()
          .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
          .setTopics("interface_total_byte")
          .setGroupId("my-group")
          .setStartingOffsets(OffsetsInitializer.latest())
          .setValueOnlyDeserializer(ConverterUtil.getConverterForMetric(IntfTotalBytesMetric.class))
          .build();
}
