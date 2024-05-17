package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.CPUUtilCDCRow;
import com.joshi.weatherinflux.common.Level1Streaming;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class CPUUtilStreaming {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(3);

    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    // IMPORTANT: Without this setting, cdc connector will not work. Cdc connector must run with
    // parallelism of 1.
    tableEnv.getConfig().set("table.exec.resource.default-parallelism", "1");

    // sj_todo type of Level1Streaming and KeyedCoProcessFunction are same as evident here.
    // Maybe we can do something about it?
    Level1Streaming<String, CPUMetric, CPUUtilCDCRow, EnrichedCPUMetric> level1 =
        new Level1Streaming<>();
    level1.setup(
        env,
        tableEnv,
        tableEnv.from(CDCSources.CPU_UTIL_DETAILS),
        null,
        new EnrichCPUUtil(),
        e -> {
          Map<String, String> tags = new HashMap<>();
          tags.put("id", e.getCpuMetric().getId());
          Map<String, Object> fields = new HashMap<>();
          fields.put("util", e.getCpuMetric().getTemp());
          InfluxDBPoint point =
              new InfluxDBPoint("cpu_util", e.getCpuMetric().getTimestamp(), tags, fields);
          return point;
        });

    env.execute("CPU Util");
  }
}
