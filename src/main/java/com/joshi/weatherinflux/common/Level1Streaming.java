package com.joshi.weatherinflux.common;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Level1Streaming<K, I extends Metric<K>, O extends OculusCDCRow<K>, E> {
  public StreamExecutionEnvironment setup(
      KafkaSource<I> kSource,
      TableDescriptor cdcTable,
      CDCRowConverter<K, O> cdcConverter,
      KeyedCoProcessFunction<K, I, O, E> keyedCoProcessFunction,
      MapFunction<E, InfluxDBPoint> mapper)
      throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    KeyedStream<I, K> inputStream =
        env.fromSource(kSource, WatermarkStrategy.noWatermarks(), "Kafka source")
            .keyBy(Metric::getKey);

    KeyedStream<O, K> cdcStream =
        tableEnv
            .toChangelogStream(tableEnv.from(cdcTable))
            .map(cdcConverter::convert)
            .keyBy(OculusCDCRow::getKey);

    DataStream<InfluxDBPoint> influxStream =
        inputStream.connect(cdcStream).process(keyedCoProcessFunction).map(mapper);

    influxStream.addSink(InfluxSink.influxDBConfig());
    return env;
  }
}
