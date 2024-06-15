package com.joshi.weatherinflux.memoryused;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.InfluxSink;
import com.joshi.weatherinflux.common.KafkaSources;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class MemoryUsedStreaming {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    DataStream<MemoryUsedMetric> ks =
        env.fromSource(
                KafkaSources.memoryUsedKafkaSource(),
                WatermarkStrategy.noWatermarks(),
                "Memory Used source")
            // keyBy will LOGICALLY split the stream based the key. Records with same key are
            // forwarded to same slot on task manager. This forwarding ensures correct state
            // sharding.
            .keyBy(MemoryUsedMetric::getDeviceId);

    DataStream<Row> deviceCDCStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CDCSources.DEVICE_CDC_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("id")).toString());

    // IMPORTANT: Both streams must have same keys for them to go to same slot on task manager.
    DataStream<InfluxDBPoint> influxStream =
        ks.connect(deviceCDCStream)
            .process(new EnrichMemoryUsed())
            .setParallelism(3)
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedMemoryUsedMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("id", value.getDeviceId());
                    tags.put("acna", value.getAcna());
                    tags.put("sponsored_by", value.getSponsoredBy());

                    Map<String, Object> fields = new HashMap<>();
                    fields.put("max_memory_used", value.getMemoryUsedMetric().getMemoryUsed());
                    fields.put("avg_memory_used", value.getMemoryUsedMetric().getMemoryUsed());
                    InfluxDBPoint point =
                        new InfluxDBPoint(
                            "device", value.getMemoryUsedMetric().getTimestamp(), tags, fields);
                    return point;
                  }
                });

    influxStream.addSink(InfluxSink.influxDBConfig()).name("Influx Sink");

    env.execute("Memory Used");
  }
}
