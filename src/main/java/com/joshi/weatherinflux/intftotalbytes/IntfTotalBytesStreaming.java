package com.joshi.weatherinflux.intftotalbytes;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class IntfTotalBytesStreaming {
  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);
  }
}
