package com.joshi.weatherinflux.common;

import java.time.Duration;
import java.time.Instant;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericFunction<
        K, I extends Metric<K>, O extends OculusCDCRow<K>, E extends EnrichedMetric<K>>
    extends KeyedCoProcessFunction<K, I, O, E> {
  private static final Logger LOG = LoggerFactory.getLogger(GenericFunction.class);
  protected transient ValueState<O> cdcRow;
  protected transient ValueState<E> prev;
  protected transient MetricToEnrichedMetric<I, O, E> mte;

  public StreamExecutionEnvironment setup(
      KafkaSource<I> kSource,
      TableDescriptor cdcTable,
      CDCRowConverter<K, O> cdcConverter,
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
        inputStream.connect(cdcStream).process(this).map(mapper);

    influxStream.addSink(InfluxSink.influxDBConfig());
    return env;
  }

  @Override
  public void processElement1(
      I value, KeyedCoProcessFunction<K, I, O, E>.Context ctx, Collector<E> out) throws Exception {
    if (cdcRow.value() != null) {
      E enriched = mte.giveE(value, cdcRow.value());
      E previousEnriched = prev.value();
      if (previousEnriched != null) {
        long prevTimestamp = previousEnriched.getMetric().getTimestamp();
        long currTimestamp = enriched.getMetric().getTimestamp();

        // sj_todo interval must be a SLA config.
        if (Duration.between(
                    Instant.ofEpochMilli(prevTimestamp), Instant.ofEpochMilli(currTimestamp))
                .toSeconds()
            > 15) {
          LOG.error("Found a gap for id {}", value.getKey());
        }
      }
      // sj_todo maybe it's better to split the gap finding and enriching metric part?
      prev.update(enriched);
      out.collect(enriched);
    } else {
      LOG.info("Metrics {} dropped because no perf data found for it", value);
    }
  }

  @Override
  public void processElement2(
      O value, KeyedCoProcessFunction<K, I, O, E>.Context ctx, Collector<E> out) throws Exception {
    switch (value.getRowKind()) {
      case UPDATE_AFTER, INSERT -> {
        LOG.info("update/insert metric {}", value);
        cdcRow.update(value);
      }
      case DELETE -> {
        LOG.info("deleted metric {}", value);
        cdcRow.update(null);
      }
      case UPDATE_BEFORE -> {
        LOG.info("Ignored cdc row UPDATE_BEFORE");
      }
    }
  }
}
