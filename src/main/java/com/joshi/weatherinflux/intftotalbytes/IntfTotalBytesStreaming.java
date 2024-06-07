package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.InfluxSink;
import com.joshi.weatherinflux.common.KafkaSources;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class IntfTotalBytesStreaming {
  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    DataStream<IntfTotalBytesMetric> ks =
        env.fromSource(
                KafkaSources.totalBytesMetricKafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Intf Total bytes totalBytesMetricKafkaSource")
            // keyBy will LOGICALLY split the stream based the key. Records with same key are
            // forwarded to same slot on task manager. This forwarding ensures correct state
            // sharding.
            .keyBy(IntfTotalBytesMetric::getId);

    DataStream<Row> cdcStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CDCSources.INTERFACE_CDC_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("id")).toString());

    // IMPORTANT: Both streams must have same keys for them to go to same slot on task manager.
    DataStream<InfluxDBPoint> influxStream =
        ks.connect(cdcStream)
            .process(new EnrichIntfTotalBytes())
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedIntfTotalBytesMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("id", value.getIntfTotalBytesMetric().getId());
                    tags.put("device_id", value.getDeviceId());

                    Map<String, Object> fields = new HashMap<>();
                    fields.put("in_totalbytes", value.getInTotalBytes());
                    fields.put("out_totalbytes", value.getOutTotalBytes());
                    fields.put("in_maxbps", value.getInMaxBps());
                    fields.put("out_maxbps", value.getOutMaxBps());

                    InfluxDBPoint point =
                        new InfluxDBPoint(
                            "interface",
                            value.getIntfTotalBytesMetric().getTimestamp(),
                            tags,
                            fields);
                    return point;
                  }
                });

    influxStream
        .addSink(InfluxSink.influxDBConfig())
        .name("Influx Sink")
        .uid(UUID.randomUUID().toString());

    env.execute("Intf Total Bytes");
  }
}
