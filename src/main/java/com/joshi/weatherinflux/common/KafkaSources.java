package com.joshi.weatherinflux.common;

import com.joshi.weatherinflux.cpuutil.CPUMetric;
import com.joshi.weatherinflux.cpuutil.CPUMetricConverter;
import com.joshi.weatherinflux.intfdiscards.IntfDiscardMetric;
import com.joshi.weatherinflux.intfdiscards.IntfDiscardMetricConverter;
import com.joshi.weatherinflux.intferrors.IntfErrorMetric;
import com.joshi.weatherinflux.intferrors.IntfErrorMetricConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesConverter;
import com.joshi.weatherinflux.intftotalbytes.IntfTotalBytesMetric;
import com.joshi.weatherinflux.memoryused.MemoryUsedMetric;
import com.joshi.weatherinflux.memoryused.MemoryUsedMetricConverter;
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

  public static KafkaSource<MemoryUsedMetric> memoryUsedKafkaSource() {
    KafkaSource<MemoryUsedMetric> source =
        KafkaSource.<MemoryUsedMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("memory_used")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new MemoryUsedMetricConverter())
            .build();
    return source;
  }

  public static KafkaSource<IntfErrorMetric> intfErrorKafkaSource() {
    KafkaSource<IntfErrorMetric> source =
        KafkaSource.<IntfErrorMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("interface_error")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new IntfErrorMetricConverter())
            .build();
    return source;
  }

  public static KafkaSource<IntfDiscardMetric> intfDiscardKafkaSource() {
    KafkaSource<IntfDiscardMetric> source =
        KafkaSource.<IntfDiscardMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("interface_discard")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new IntfDiscardMetricConverter())
            .build();
    return source;
  }
}
