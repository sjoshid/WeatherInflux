package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.KafkaSources;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;

public class Main {

  public static void main(String[] args) throws Exception {
    CPUUtilProcessFunction fun = new CPUUtilProcessFunction();
    try (StreamExecutionEnvironment env =
        fun.setup(
            KafkaSources.cpuMetricKafkaSource,
            CDCSources.CPU_UTIL_DETAILS,
            null,
            e -> {
              Map<String, String> tags = new HashMap<>();
              tags.put("id", e.getCpuMetric().getId());
              Map<String, Object> fields = new HashMap<>();
              fields.put("util", e.getCpuMetric().getTemp());
              InfluxDBPoint point =
                  new InfluxDBPoint("cpu_util", e.getCpuMetric().getTimestamp(), tags, fields);
              return point;
            })) {

      env.execute("CPU Util");
    }
  }
}
