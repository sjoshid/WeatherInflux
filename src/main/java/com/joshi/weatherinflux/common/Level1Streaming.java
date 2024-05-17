package com.joshi.weatherinflux.common;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Level1Streaming<K, I extends Metric<K>, O extends OculusCDCRow<K>, E> {
  KafkaSourceInst<K, I> kafkaSource; // comment

  public void setup(
      StreamExecutionEnvironment env,
      StreamTableEnvironment tableEnv,
      Table cdcTable,
      CDCRowConverter<K, O> cdcConverter,
      KeyedCoProcessFunction<K, I, O, E> keyedCoProcessFunction,
      MapFunction<E, InfluxDBPoint> mapper) {
    KeyedStream<I, K> inputStream =
        env.fromSource(
                kafkaSource.kSource, WatermarkStrategy.noWatermarks(), kafkaSource.sourceName)
            .keyBy(Metric::getKey);

    KeyedStream<O, K> cdcStream =
        tableEnv.toChangelogStream(cdcTable).map(cdcConverter::convert).keyBy(OculusCDCRow::getKey);

    DataStream<InfluxDBPoint> influxStream =
        inputStream.connect(cdcStream).process(keyedCoProcessFunction).map(mapper);

    influxStream.addSink(InfluxSink.influxDBConfig());
  }
}
