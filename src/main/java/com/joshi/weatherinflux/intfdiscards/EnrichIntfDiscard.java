package com.joshi.weatherinflux.intfdiscards;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnrichIntfDiscard
    extends KeyedCoProcessFunction<String, IntfDiscardMetric, Row, EnrichedIntfDiscardMetric> {

  private static final Logger LOG = LoggerFactory.getLogger(EnrichIntfDiscard.class);
  private transient ValueState<Row> cdcRow;
  private ValueState<EnrichedIntfDiscardMetric> prev;

  @Override
  public void processElement1(
      IntfDiscardMetric value,
      KeyedCoProcessFunction<String, IntfDiscardMetric, Row, EnrichedIntfDiscardMetric>.Context ctx,
      Collector<EnrichedIntfDiscardMetric> out)
      throws Exception {
    Row detail = cdcRow.value();
    if (detail != null) {
      // Output the enriched metric with inventory details.
      EnrichedIntfDiscardMetric enriched = new EnrichedIntfDiscardMetric(value);
      String deviceId = Objects.requireNonNull(detail.getField("device_id")).toString();
      enriched.setDeviceId(deviceId);

      EnrichedIntfDiscardMetric previousEnriched = prev.value();
      if (previousEnriched != null) {
        long prevTimestamp = previousEnriched.getIntfDiscardMetric().getTimestamp();
        long currTimestamp = enriched.getIntfDiscardMetric().getTimestamp();

        // sj_todo interval must be a SLA config.
        if (Duration.between(
                    Instant.ofEpochMilli(prevTimestamp), Instant.ofEpochMilli(currTimestamp))
                .toSeconds()
            > 15) {
          LOG.warn("Found a gap for id {}", value.getId());
        }
      }
      // sj_todo maybe it's better to split the gap finding and enriching metric part?
      prev.update(enriched);
      out.collect(enriched);
    } else {
      LOG.error("Metrics {} dropped because no perf data found for it", value);
    }
  }

  @Override
  public void processElement2(
      Row value,
      KeyedCoProcessFunction<String, IntfDiscardMetric, Row, EnrichedIntfDiscardMetric>.Context ctx,
      Collector<EnrichedIntfDiscardMetric> out)
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

  @Override
  public void open(OpenContext openContext) throws Exception {
    cdcRow = getRuntimeContext().getState(new ValueStateDescriptor<>("Intf. CDC state", Row.class));
    prev =
        getRuntimeContext()
            .getState(
                new ValueStateDescriptor<>("Enriched CPU Util state", EnrichedIntfDiscardMetric.class));
  }
}
