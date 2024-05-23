package com.joshi.weatherinflux.intftotalbytes;

import java.time.Duration;
import java.time.Instant;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnrichIntfTotalBytes
    extends KeyedCoProcessFunction<
        String, IntfTotalBytesMetric, Row, EnrichedIntfTotalBytesMetric> {

  private static final Logger LOG = LoggerFactory.getLogger(EnrichIntfTotalBytes.class);
  private transient ValueState<Row> cdcRow;
  private ValueState<EnrichedIntfTotalBytesMetric> prev;

  @Override
  public void open(OpenContext openContext) throws Exception {
    cdcRow = getRuntimeContext().getState(new ValueStateDescriptor<>("perfCDCData", Row.class));
    prev =
        getRuntimeContext()
            .getState(
                new ValueStateDescriptor<>(
                    "Enriched Intf total bytes state", EnrichedIntfTotalBytesMetric.class));
  }

  @Override
  public void processElement1(
      IntfTotalBytesMetric value,
      KeyedCoProcessFunction<String, IntfTotalBytesMetric, Row, EnrichedIntfTotalBytesMetric>
              .Context
          ctx,
      Collector<EnrichedIntfTotalBytesMetric> out)
      throws Exception {
    Row detail = cdcRow.value();
    if (detail != null) {
      // Output the enriched metric with inventory details.
      EnrichedIntfTotalBytesMetric enriched = new EnrichedIntfTotalBytesMetric(value);
      EnrichedIntfTotalBytesMetric previousEnriched = prev.value();
      if (previousEnriched != null) {
        long prevTimestamp = previousEnriched.getIntfTotalBytesMetric().getTimestamp();
        long currTimestamp = enriched.getIntfTotalBytesMetric().getTimestamp();

        // sj_todo interval must be a SLA config.
        if (Duration.between(
                    Instant.ofEpochMilli(prevTimestamp), Instant.ofEpochMilli(currTimestamp))
                .toSeconds()
            > 15) {
          LOG.error("Found a gap for id {}", value.getId());
        }

        if (prevTimestamp < currTimestamp) {
          final Duration dur =
              Duration.between(
                  Instant.ofEpochMilli(prevTimestamp), Instant.ofEpochMilli(currTimestamp));
          float totalBytes = value.getInBytes() + value.getOutBytes();
          float maxBps = totalBytes / dur.toSeconds();
          enriched.setMaxBps(maxBps);
          out.collect(enriched);
        } else {
          LOG.warn("Not possible for prev poll time to be >= curr poll time.");
        }
      }
      // sj_todo maybe it's better to split the gap finding and enriching metric part?
      prev.update(enriched);
    } else {
      LOG.info("Metrics {} dropped because no perf data found for it", value);
    }
  }

  @Override
  public void processElement2(
      Row value,
      KeyedCoProcessFunction<String, IntfTotalBytesMetric, Row, EnrichedIntfTotalBytesMetric>
              .Context
          ctx,
      Collector<EnrichedIntfTotalBytesMetric> out)
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