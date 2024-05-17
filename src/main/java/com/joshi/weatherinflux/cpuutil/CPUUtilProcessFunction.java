package com.joshi.weatherinflux.cpuutil;

import com.joshi.weatherinflux.common.GenericFunction;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueStateDescriptor;

public class CPUUtilProcessFunction
    extends GenericFunction<String, CPUMetric, CPUUtilCDCRow, EnrichedCPUMetric> {

  @Override
  public void open(OpenContext openContext) throws Exception {
    cdcRow =
        getRuntimeContext()
            .getState(new ValueStateDescriptor<>("perfCDCData", CPUUtilCDCRow.class));
    prev =
        getRuntimeContext()
            .getState(
                new ValueStateDescriptor<>("Enriched CPU Util state", EnrichedCPUMetric.class));
  }
}
