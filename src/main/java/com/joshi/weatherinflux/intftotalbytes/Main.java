package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.KafkaSources;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;

public class Main {
  public static void main(String[] args) throws Exception {
    IntfTotalBytesProcessFunction fun = new IntfTotalBytesProcessFunction();
    try (StreamExecutionEnvironment env =
        fun.setup(
            KafkaSources.intfTotalBytesMetricKafkaSource,
            CDCSources.TOTAL_BYTES_CDC,
            null,
            e -> {
              Map<String, String> tags = new HashMap<>();
              tags.put("id", e.getIntfTotalBytesMetric().getId());
              Map<String, Object> fields = new HashMap<>();
              fields.put("maxBps", e.getMaxBps());
              InfluxDBPoint point =
                  new InfluxDBPoint(
                      "interface", e.getIntfTotalBytesMetric().getTimestamp(), tags, fields);
              return point;
            })) {
      env.execute("Intf Total Bytes");
    }
  }
}
