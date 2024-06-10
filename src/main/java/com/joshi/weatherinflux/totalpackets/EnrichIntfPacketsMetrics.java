package com.joshi.weatherinflux.totalpackets;

import java.util.Objects;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnrichIntfPacketsMetrics
    extends KeyedCoProcessFunction<
        String, EnrichedIntfTotalPacketsMetric, Row, EnrichedIntfTotalPacketsMetric> {

  private static final Logger LOG = LoggerFactory.getLogger(EnrichIntfPacketsMetrics.class);
  private transient ValueState<Row> cdcRow;
  private ValueState<EnrichedIntfTotalPacketsMetric> prev;

  @Override
  public void open(OpenContext openContext) throws Exception {
    cdcRow = getRuntimeContext().getState(new ValueStateDescriptor<>("perfCDCData", Row.class));
    prev =
        getRuntimeContext()
            .getState(
                new ValueStateDescriptor<>(
                    "Prev enriched Intf total packets state",
                    EnrichedIntfTotalPacketsMetric.class));
  }

  @Override
  public void processElement1(
      EnrichedIntfTotalPacketsMetric value,
      KeyedCoProcessFunction<
                  String, EnrichedIntfTotalPacketsMetric, Row, EnrichedIntfTotalPacketsMetric>
              .Context
          ctx,
      Collector<EnrichedIntfTotalPacketsMetric> out)
      throws Exception {
    Row detail = cdcRow.value();
    if (detail != null) {
      EnrichedIntfTotalPacketsMetric prevMetric = prev.value();
      if (prevMetric == null) {
        // sj_todo make sure you enrich using cdc before updating.
        prev.update(value);
        LOG.info("First total packet metric {} which will NOT be created in Influx.", value);
      } else {
        // sj_todo is it a good idea to create new object here?
        EnrichedIntfTotalPacketsMetric newMetric =
            new EnrichedIntfTotalPacketsMetric(
                value.getId(),
                value.getTimestamp(),
                // IMPORTANT: Total packets is a counter that our current NMS provides.
                // Pray that the diff is positive.
                value.getInTotalPackets() - prevMetric.getInTotalPackets(),
                value.getOutTotalPackets() - prevMetric.getOutTotalPackets());
        String deviceId = Objects.requireNonNull(detail.getField("device_id")).toString();
        newMetric.setDeviceId(deviceId);
        LOG.info("Collected total packet metric {} for influx", newMetric);
        out.collect(newMetric);
        prev.update(newMetric);
      }
    } else {
      LOG.error("Metrics {} dropped because no perf data found for it", value);
    }
  }

  public void processElement2(
      Row value,
      KeyedCoProcessFunction<
                  String, EnrichedIntfTotalPacketsMetric, Row, EnrichedIntfTotalPacketsMetric>
              .Context
          ctx,
      Collector<EnrichedIntfTotalPacketsMetric> out)
      throws Exception {
    switch (value.getKind()) {
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
