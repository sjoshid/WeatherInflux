package com.joshi.weatherinflux.totalpackets;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.InfluxSink;
import com.joshi.weatherinflux.common.KafkaSources;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class IntfTotalPacketsStreaming {
  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    // timestamp is part of key because we want metric collected at same timestamp for same intf. id
    DataStream<IntfPacketsMetric> unicastSource =
        env.fromSource(
                KafkaSources.unicastMetricKafkaSource,
                WatermarkStrategy.noWatermarks(),
                "UnicastMetric Kafka source")
            .uid(UUID.randomUUID().toString());

    DataStream<IntfPacketsMetric> multicastSource =
        env.fromSource(
                KafkaSources.multicastMetricKafkaSource,
                WatermarkStrategy.noWatermarks(),
                "MulticastMetric Kafka source")
            .uid(UUID.randomUUID().toString());

    DataStream<IntfPacketsMetric> broadcastSource =
        env.fromSource(
                KafkaSources.broadcastMetricKafkaSource,
                WatermarkStrategy.noWatermarks(),
                "BroadcastMetric Kafka source")
            .uid(UUID.randomUUID().toString());

    DataStream<Row> cdcStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CDCSources.INTERFACE_CDC_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("id")).toString());

    DataStream<InfluxDBPoint> influxStream =
        unicastSource
            .union(multicastSource, broadcastSource)
            .keyBy(
                new KeySelector<IntfPacketsMetric, Tuple2<String, Long>>() {
                  @Override
                  public Tuple2<String, Long> getKey(IntfPacketsMetric value) throws Exception {
                    return new Tuple2<String, Long>(value.getId(), value.getTimestamp());
                  }
                })
            .process(new CalculateTotalPacketsProcessFunction())
            .keyBy(EnrichedIntfTotalPacketsMetric::getId)
            .connect(cdcStream)
            .process(new EnrichIntfPacketsMetrics())
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedIntfTotalPacketsMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("id", value.getId());
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("inTotalPackets", value.getInTotalPackets());
                    fields.put("outTotalPackets", value.getOutTotalPackets());
                    InfluxDBPoint point =
                        new InfluxDBPoint("interface", value.getTimestamp(), tags, fields);
                    return point;
                  }
                });

    influxStream
        .addSink(InfluxSink.influxDBConfig())
        .name("Influx Sink")
        .uid(UUID.randomUUID().toString());

    env.execute("Intf Total Packets");
  }
}
