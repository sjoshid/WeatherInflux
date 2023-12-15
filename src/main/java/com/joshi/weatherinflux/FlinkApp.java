package com.joshi.weatherinflux;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBConfig;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBSink;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class FlinkApp {

  public static final TableDescriptor CPU_UTIL_DETAILS =
      TableDescriptor.forConnector("mysql-cdc")
          .schema(
              Schema.newBuilder()
                  .column("nms_region", DataTypes.SMALLINT().notNull())
                  .column("nms_perf_key", DataTypes.STRING().notNull())
                  .column("nms_device_id", DataTypes.STRING().notNull())
                  .column("nms_device_name", DataTypes.STRING().notNull())
                  .column("nms_instance_description", DataTypes.STRING().notNull())
                  .column("nms_ds0_description", DataTypes.STRING().notNull())
                  .column("nms_ds1_description", DataTypes.STRING().notNull())
                  .column("inv_acna", DataTypes.STRING().notNull())
                  .columnByExpression("t_proctime", "PROCTIME()")
                  .primaryKey("nms_region", "nms_perf_key")
                  .build())
          .option("hostname", "mm-mariadb-for-auto-metrics")
          .option("port", "3306")
          .option("username", "boss")
          .option("password", "IMBOSS")
          .option("database-name", "Netreo")
          .option("table-name", "cpu_util_details")
          .option("server-id", "5800-5900")
          .option("heartbeat.interval", "1s")
          .build();

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    KafkaSource<CPUMetric> source =
        KafkaSource.<CPUMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("cpu_utilization")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new CPUMetricConverter())
            .build();
    InfluxDBConfig influxDBConfig =
        InfluxDBConfig.builder("http://mm-influxdb:8086", "boss", "C0mpl3X", "raw")
            .batchActions(2000)
            .flushDuration(2000, TimeUnit.MILLISECONDS)
            .enableGzip(true)
            .build();

    DataStream<CPUMetric> ks =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "CPU util source")
            // keyBy will LOGICALLY split the stream based the key. Records with same key are
            // forwarded to same slot on task manager. This forwarding ensures correct state
            // sharding.
            .keyBy(CPUMetric::getId);

    DataStream<Row> cpuUtilCDCStream =
        tableEnv
            .toChangelogStream(tableEnv.from(CPU_UTIL_DETAILS))
            .keyBy(r -> Objects.requireNonNull(r.getField("nms_perf_key")).toString());
    tableEnv.createTemporaryView("hello", cpuUtilCDCStream);

    // IMPORTANT: Both streams must have same keys for them to go to same slot on task manager.
    DataStream<InfluxDBPoint> enrichedCPUMetricsInfluxPoint =
        ks.connect(cpuUtilCDCStream)
            .process(new DeviceEnrichment())
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedCPUMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("perf_key", value.getCpuMetric().getId());
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("util", value.getCpuMetric().getTemp());
                    InfluxDBPoint point =
                        new InfluxDBPoint(
                            "cpu_util", value.getCpuMetric().getTimestamp(), tags, fields);
                    return point;
                  }
                });

    enrichedCPUMetricsInfluxPoint.addSink(new InfluxDBSink(influxDBConfig)).name("Influx Sink");

    env.execute("With cdc v 3.0.0");
  }
}
