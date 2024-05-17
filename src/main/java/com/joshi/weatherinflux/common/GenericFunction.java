package com.joshi.weatherinflux.common;

import java.time.Duration;
import java.time.Instant;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericFunction<
        K, I extends Metric<K>, O extends OculusCDCRow<K>, E extends EnrichedMetric<K>>
    extends KeyedCoProcessFunction<K, I, O, E> {
  private static final Logger LOG = LoggerFactory.getLogger(GenericFunction.class);
  protected transient ValueState<O> cdcRow;
  protected transient ValueState<E> prev;
  protected transient MetricToEnrichedMetric<I, O, E> mte;

  @Override
  public void processElement1(
      I value, KeyedCoProcessFunction<K, I, O, E>.Context ctx, Collector<E> out) throws Exception {
    if (cdcRow.value() != null) {
      E enriched = mte.giveE(value, cdcRow.value());
      E previousEnriched = prev.value();
      if (previousEnriched != null) {
        long prevTimestamp = previousEnriched.getMetric().getTimestamp();
        long currTimestamp = enriched.getMetric().getTimestamp();

        // sj_todo interval must be a SLA config.
        if (Duration.between(
                    Instant.ofEpochMilli(prevTimestamp), Instant.ofEpochMilli(currTimestamp))
                .toSeconds()
            > 15) {
          LOG.error("Found a gap for id {}", value.getKey());
        }
      }
      // sj_todo maybe it's better to split the gap finding and enriching metric part?
      prev.update(enriched);
      out.collect(enriched);
    } else {
      LOG.info("Metrics {} dropped because no perf data found for it", value);
    }
  }

  @Override
  public void processElement2(
      O value, KeyedCoProcessFunction<K, I, O, E>.Context ctx, Collector<E> out) throws Exception {
    switch (value.getRowKind()) {
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
