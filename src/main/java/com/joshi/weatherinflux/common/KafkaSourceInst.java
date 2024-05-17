package com.joshi.weatherinflux.common;

import org.apache.flink.connector.kafka.source.KafkaSource;

public class KafkaSourceInst<K, I extends Metric<K>> {
  public KafkaSource<I> kSource;
  public String sourceName;
}
