package com.joshi.weatherinflux.intftotalbytes;

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

public class IntfTotalBytesStreaming {
  public static final TableDescriptor TOTAL_BYTES_CDC =
      TableDescriptor.forConnector("mysql-cdc")
          .schema(
              Schema.newBuilder()
                  .column("nms_region", DataTypes.SMALLINT().notNull())
                  .column("id", DataTypes.STRING().notNull())
                  .column("intf_id", DataTypes.STRING().notNull())
                  .column("device_id", DataTypes.STRING().notNull())
                  .column("nms_device_name", DataTypes.STRING().notNull())
                  .column("nms_instance_description", DataTypes.STRING().notNull())
                  .column("nms_ds0_description", DataTypes.STRING().notNull())
                  .column("nms_ds1_description", DataTypes.STRING().nullable())
                  .column("inv_acna", DataTypes.STRING().notNull())
                  .columnByExpression("t_proctime", "PROCTIME()")
                  .primaryKey("nms_region", "device_id", "intf_id")
                  .build())
          .option("hostname", "mm-mariadb-for-auto-metrics")
          .option("port", "3306")
          .option("username", "boss")
          .option("password", "IMBOSS")
          .option("database-name", "Netreo")
          .option("table-name", "intf_total_bytes_cdc_details")
          .option("heartbeat.interval", "1s")
          .build();

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    KafkaSource<IntfTotalBytesMetric> source =
        KafkaSource.<IntfTotalBytesMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("interface_total_byte")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new IntfTotalBytesConverter())
            .build();
    InfluxDBConfig influxDBConfig =
        InfluxDBConfig.builder("http://mm-influxdb:8086", "boss", "C0mpl3X", "raw")
            .batchActions(2000)
            .flushDuration(2000, TimeUnit.MILLISECONDS)
            .enableGzip(true)
            .build();

    DataStream<IntfTotalBytesMetric> ks =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Intf Total bytes source")
            // keyBy will LOGICALLY split the stream based the key. Records with same key are
            // forwarded to same slot on task manager. This forwarding ensures correct state
            // sharding.
            .keyBy(IntfTotalBytesMetric::getId);

    DataStream<Row> cpuUtilCDCStream =
        tableEnv
            .toChangelogStream(tableEnv.from(TOTAL_BYTES_CDC))
            .keyBy(
                r -> {
                  final String[] id = {
                    Objects.requireNonNull(r.getField("nms_region")).toString(),
                    Objects.requireNonNull(r.getField("device_id")).toString(),
                    Objects.requireNonNull(r.getField("intf_id")).toString()
                  };

                  return String.join("_", id);
                });

    // IMPORTANT: Both streams must have same keys for them to go to same slot on task manager.
    DataStream<InfluxDBPoint> influxStream =
        ks.connect(cpuUtilCDCStream)
            .process(new EnrichIntfTotalBytes())
            .map(
                new RichMapFunction<>() {
                  @Override
                  public InfluxDBPoint map(EnrichedIntfTotalBytesMetric value) throws Exception {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("id", value.getIntfTotalBytesMetric().getId());
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("maxBps", value.getMaxBps());
                    InfluxDBPoint point =
                        new InfluxDBPoint(
                            "interface",
                            value.getIntfTotalBytesMetric().getTimestamp(),
                            tags,
                            fields);
                    return point;
                  }
                });

    influxStream.addSink(new InfluxDBSink(influxDBConfig)).name("Influx Sink");

    env.execute("Intf Total Bytes");
  }
}
