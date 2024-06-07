package com.joshi.weatherinflux.cpuutil;

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

public class CPUUtilStreaming {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    DataStream<CPUMetric> ks =
        env.fromSource(
                KafkaSources.cPUUtilKafkaSource(),
                WatermarkStrategy.noWatermarks(),
                "CPU util source")
            // keyBy will LOGICALLY split the stream based the key. Records with same key are
            // forwarded to same slot on task manager. This forwarding ensures correct state
            // sharding.
            .keyBy(CPUMetric::getId);

    DataStream<Row> cpuUtilCDCStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CDCSources.DEVICE_CDC_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("id")).toString());

    // IMPORTANT: Both streams must have same keys for them to go to same slot on task manager.
    DataStream<InfluxDBPoint> influxStream =
        ks.connect(cpuUtilCDCStream)
            .process(new EnrichCPUUtil())
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedCPUMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("id", value.getCpuMetric().getId());
                    tags.put("acna", value.getAcna());
                    tags.put("sponsored_by", value.getSponsoredBy());
                    tags.put("country", value.getCountry());

                    Map<String, Object> fields = new HashMap<>();
                    fields.put("max_load", value.getCpuMetric().getUtil());
                    fields.put("avg_load", value.getCpuMetric().getUtil());
                    InfluxDBPoint point =
                        new InfluxDBPoint("device", value.getCpuMetric().getTimestamp(), tags, fields);
                    return point;
                  }
                });

    influxStream.addSink(InfluxSink.influxDBConfig()).name("Influx Sink");

    env.execute("CPU Util");
  }
}
