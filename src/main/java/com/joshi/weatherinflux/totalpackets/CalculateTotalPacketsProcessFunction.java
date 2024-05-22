package com.joshi.weatherinflux.totalpackets;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CalculateTotalPacketsProcessFunction
    extends KeyedProcessFunction<
        Tuple2<String, Long>, IntfPacketsMetric, EnrichedIntfTotalPacketsMetric> {

  private static final Logger LOG =
      LoggerFactory.getLogger(CalculateTotalPacketsProcessFunction.class);

  private transient ValueState<Integer> mask;
  private transient ValueState<Float> currInTotal;
  private transient ValueState<Float> currOutTotal;
  private transient ValueState<Long> timer;

  @Override
  public void open(OpenContext openContext) throws Exception {
    mask = getRuntimeContext().getState(new ValueStateDescriptor<>("Mask", Integer.class));
    currInTotal =
        getRuntimeContext()
            .getState(new ValueStateDescriptor<>("Current in-total value", Float.class));
    currOutTotal =
        getRuntimeContext()
            .getState(new ValueStateDescriptor<>("Current out-total value", Float.class));
    timer = getRuntimeContext().getState(new ValueStateDescriptor<>("Time to wait", Long.class));
    mask.update(0x000);
    currInTotal.update(.0f);
    currOutTotal.update(.0f);
  }

  @Override
  public void processElement(
      IntfPacketsMetric value,
      KeyedProcessFunction<Tuple2<String, Long>, IntfPacketsMetric, EnrichedIntfTotalPacketsMetric>
              .Context
          ctx,
      Collector<EnrichedIntfTotalPacketsMetric> out)
      throws Exception {
    int before = mask.value();
    final int after = value.getType().getValue() | before;
    if (after == before) {
      // huh?
      LOG.error(
          "Received repeated (same intf, same ts and same MetricType) metric {}. I will drop it.",
          value);
    } else if (after == 0x111) {
      // found all three. Add all up.
      float inTotalFinal = currInTotal.value() + value.getInPackets();
      float outTotalFinal = currOutTotal.value() + value.getInPackets();

      out.collect(
          new EnrichedIntfTotalPacketsMetric(
              value.getId(), value.getTimestamp(), inTotalFinal, outTotalFinal));

      // Clear is right. Not reset. Because we include ts in key.
      mask.clear();
      currInTotal.clear();
      currOutTotal.clear();

      ctx.timerService().deleteProcessingTimeTimer(timer.value());
      timer.clear();
    } else {
      // More to come.
      currInTotal.update(currInTotal.value() + value.getInPackets());
      currOutTotal.update(currOutTotal.value() + value.getInPackets());
      mask.update(after);

      // sj_todo change this.
      long futureTime = ctx.timerService().currentProcessingTime() + 10_000;

      timer.update(futureTime);
      ctx.timerService().registerProcessingTimeTimer(futureTime);
    }
  }

  public void onTimer(
      long timestamp, OnTimerContext ctx, Collector<EnrichedIntfTotalPacketsMetric> out)
      throws Exception {
    LOG.error(
        "Did not get all 3 types of packets in time to calculate total packets. I got {}",
        mask.value());

    // Clear is right. Not reset. Because we include ts in key.
    mask.clear();
    currInTotal.clear();
    currOutTotal.clear();
    timer.clear();
  }
}
