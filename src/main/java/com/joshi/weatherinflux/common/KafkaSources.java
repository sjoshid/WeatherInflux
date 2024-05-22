package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import com.joshi.weatherinflux.cpuutil.CPUMetricConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesMetric;
import com.joshi.weatherinflux.totalpackets.BroadcastConverter;
import com.joshi.weatherinflux.totalpackets.IntfPacketsMetric;
import com.joshi.weatherinflux.totalpackets.MulticastConverter;
import com.joshi.weatherinflux.totalpackets.UnicastConverter;
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
  public static KafkaSource<IntfPacketsMetric> unicastMetricKafkaSource =
      KafkaSource.<IntfPacketsMetric>builder()
          .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
          .setTopics("interface_unicast_packet")
          .setGroupId("my-group")
          .setStartingOffsets(OffsetsInitializer.latest())
          .setValueOnlyDeserializer(new UnicastConverter())
          .build();
  public static KafkaSource<IntfPacketsMetric> multicastMetricKafkaSource =
      KafkaSource.<IntfPacketsMetric>builder()
          .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
          .setTopics("interface_multicast_packet")
          .setGroupId("my-group")
          .setStartingOffsets(OffsetsInitializer.latest())
          .setValueOnlyDeserializer(new MulticastConverter())
          .build();
  public static KafkaSource<IntfPacketsMetric> broadcastMetricKafkaSource =
      KafkaSource.<IntfPacketsMetric>builder()
          .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
          .setTopics("interface_broadcast_packet")
          .setGroupId("my-group")
          .setStartingOffsets(OffsetsInitializer.latest())
          .setValueOnlyDeserializer(new BroadcastConverter())
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
