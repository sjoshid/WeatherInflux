package com.joshi.weatherinflux.intftotalbytes;

import com.joshi.weatherinflux.common.GenericFunction;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueStateDescriptor;

public class IntfTotalBytesProcessFunction
    extends GenericFunction<
        String, IntfTotalBytesMetric, IntfTotalBytesCDCRow, EnrichedIntfTotalBytesMetric> {

  @Override
  public void open(OpenContext openContext) throws Exception {
    cdcRow =
        getRuntimeContext()
            .getState(new ValueStateDescriptor<>("perfCDCData", IntfTotalBytesCDCRow.class));
    prev =
        getRuntimeContext()
            .getState(
                new ValueStateDescriptor<>(
                    "Enriched Intf total bytes state", EnrichedIntfTotalBytesMetric.class));
  }
}
