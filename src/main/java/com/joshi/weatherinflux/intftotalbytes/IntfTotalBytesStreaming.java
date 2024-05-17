package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.CDCSources;
import com.joshi.weatherinflux.common.KafkaSources;
import com.joshi.weatherinflux.common.Level1Streaming;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.InfluxDBPoint;

public class IntfTotalBytesStreaming {
  public static void main(String[] args) throws Exception {
    Level1Streaming<
            String, IntfTotalBytesMetric, IntfTotalBytesCDCRow, EnrichedIntfTotalBytesMetric>
        level1 = new Level1Streaming<>();
    try (StreamExecutionEnvironment env =
        level1.setup(
            KafkaSources.intfTotalBytesMetricKafkaSource,
            CDCSources.TOTAL_BYTES_CDC,
            null,
            new IntfTotalBytesProcessFunction(),
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
