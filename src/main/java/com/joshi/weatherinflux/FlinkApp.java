package com.joshi.weatherinflux;

import java.time.Duration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.util.Collector;

public class FlinkApp {
  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    KafkaSource<CPUMetric> source =
        KafkaSource.<CPUMetric>builder()
            .setBootstrapServers("mm-broker-1:29092,mm-broker-2:29092,mm-broker-3:29092")
            .setTopics("cpu_utilization")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new CPUMetricConverter())
            .build();
    DataStream<Tuple3<String, Long, Float>> ks =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
            .map(e -> new Tuple3<>(e.id(), e.timestamp(), e.temp()))
            .returns(TypeInformation.of(new TypeHint<Tuple3<String, Long, Float>>() {}))
            .keyBy(e -> e.f0)
            .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(5)))
            .maxBy(2);

    ks.print();
    env.execute("Max temp by 5 sec tumbling window");
  }

  public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
    @Override
    public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
      if (sentence.contains("sujit")) {
        out.collect(new Tuple2<>("sujit", 1));
      } else if (sentence.contains("joshi")) {
        out.collect(new Tuple2<>("joshi", 1));
      }
    }
  }
}
