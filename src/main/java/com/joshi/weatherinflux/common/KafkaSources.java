package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import com.joshi.weatherinflux.cpuutil.CPUMetricConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesMetric;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class KafkaSources {
  public static KafkaSource<IntfTotalBytesMetric> totalBytesMetricKafkaSource =
      KafkaSource.<IntfTotalBytesMetric>builder()
          .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
          .setTopics("interface_total_byte")
          .setGroupId("my-group")
          .setStartingOffsets(OffsetsInitializer.latest())
          .setValueOnlyDeserializer(new IntfTotalBytesConverter())
          .build();

  public static KafkaSource<CPUMetric> cPUUtilKafkaSource() {
    KafkaSource<CPUMetric> source =
        KafkaSource.<CPUMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("cpu_utilization")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new CPUMetricConverter())
            .build();
    return source;
  }
}
