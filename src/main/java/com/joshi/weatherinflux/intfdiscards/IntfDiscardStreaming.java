package com.joshi.weatherinflux.intfdiscards;

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

public class IntfDiscardStreaming {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    DataStream<IntfDiscardMetric> ks =
        env.fromSource(
                KafkaSources.intfDiscardKafkaSource(),
                WatermarkStrategy.noWatermarks(),
                "Intf Discard source")
            // keyBy will LOGICALLY split the stream based the key. Records with same key are
            // forwarded to same slot on task manager. This forwarding ensures correct state
            // sharding.
            .keyBy(IntfDiscardMetric::getId);

    DataStream<Row> intfCDCStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CDCSources.INTERFACE_CDC_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("id")).toString());

    DataStream<Row> deviceCDCStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CDCSources.DEVICE_CDC_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("id")).toString());

    // IMPORTANT: Both streams must have same keys for them to go to same slot on task manager.
    DataStream<InfluxDBPoint> influxStream =
        ks.connect(intfCDCStream)
            .process(new EnrichIntfDiscard())
            .keyBy(EnrichedIntfDiscardMetric::getDeviceId)
            .connect(deviceCDCStream)
            .process(new EnrichIntfDiscardWithDeviceDetails())
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedIntfDiscardMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("id", value.getIntfDiscardMetric().getId());
                    tags.put("device_id", value.getDeviceId());
                    tags.put("acna", value.getAcna());
                    tags.put("sponsored_by", value.getSponsoredBy());

                    Map<String, Object> fields = new HashMap<>();
                    fields.put("in_discards", value.getIntfDiscardMetric().getInDiscards());
                    fields.put("out_discards", value.getIntfDiscardMetric().getOutDiscards());
                    InfluxDBPoint point =
                        new InfluxDBPoint(
                            "interface", value.getIntfDiscardMetric().getTimestamp(), tags, fields);
                    return point;
                  }
                });

    influxStream.addSink(InfluxSink.influxDBConfig()).name("Influx Sink");

    env.execute("Intf Discard");
  }
}
