package com.joshi.weatherinflux.totalpackets;

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
    EnrichedIntfTotalPacketsMetric prevMetric = prev.value();
    if (prevMetric == null) {
      // sj_todo make sure you enrich before updating.
      prev.update(value);
    } else {
      // sj_todo is it a good idea to create new object here?
      EnrichedIntfTotalPacketsMetric newMetric =
          new EnrichedIntfTotalPacketsMetric(
              prevMetric.getId(),
              prevMetric.getTimestamp(),
              value.getInTotalPackets() - prevMetric.getInTotalPackets(),
              value.getOutTotalPackets() - prevMetric.getOutTotalPackets());
      out.collect(newMetric);
      prev.update(newMetric);
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
