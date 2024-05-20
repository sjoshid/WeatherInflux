package com.joshi.weatherinflux.common;

import java.util.concurrent.TimeUnit;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBConfig;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBSink;

public class InfluxSink {
  public static InfluxDBSink influxDBSink =
      new InfluxDBSink(
          InfluxDBConfig.builder("http://mm-influxdb:8086", "boss", "C0mpl3X", "raw")
              .batchActions(2000)
              .flushDuration(2000, TimeUnit.MILLISECONDS)
              .enableGzip(true)
              .build());
}
