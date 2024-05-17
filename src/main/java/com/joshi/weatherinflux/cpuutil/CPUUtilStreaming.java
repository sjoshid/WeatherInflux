package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.KafkaSources;
import com.joshi.weatherinflux.common.Level1Streaming;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;

public class CPUUtilStreaming {

  public static void main(String[] args) throws Exception {
    Level1Streaming<String, CPUMetric, CPUUtilCDCRow, EnrichedCPUMetric> level1 =
        new Level1Streaming<>();
    try (StreamExecutionEnvironment env =
        level1.setup(
            KafkaSources.cpuMetricKafkaSource,
            CDCSources.CPU_UTIL_DETAILS,
            null,
            new CPUUtilProcessFunction(),
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
