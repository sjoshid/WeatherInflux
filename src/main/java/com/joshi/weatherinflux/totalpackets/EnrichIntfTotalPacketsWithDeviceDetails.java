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

public class EnrichIntfTotalPacketsWithDeviceDetails
    extends KeyedCoProcessFunction<
        String, EnrichedIntfTotalPacketsMetric, Row, EnrichedIntfTotalPacketsMetric> {

  private static final Logger LOG =
      LoggerFactory.getLogger(EnrichIntfTotalPacketsWithDeviceDetails.class);
  private transient ValueState<Row> deviceCDCRow;

  @Override
  public void processElement1(
      EnrichedIntfTotalPacketsMetric value,
      KeyedCoProcessFunction<
                  String, EnrichedIntfTotalPacketsMetric, Row, EnrichedIntfTotalPacketsMetric>
              .Context
          ctx,
      Collector<EnrichedIntfTotalPacketsMetric> out)
      throws Exception {
    Row detail = deviceCDCRow.value();
    if (detail != null) {
      value.setAcna(Objects.requireNonNull(detail.getField("inv_acna")).toString());
      value.setSponsoredBy(Objects.requireNonNull(detail.getField("inv_sponsored_by")).toString());
      out.collect(value);
    } else {
      LOG.info(
          "Metrics dropped because no device found for interface total bytes metric {}.", value);
    }
  }

  @Override
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
        deviceCDCRow.update(value);
      }
      case DELETE -> {
        LOG.info("deleted metric {}", value);
        deviceCDCRow.update(null);
      }
      case UPDATE_BEFORE -> {
        LOG.info("Ignored cdc row UPDATE_BEFORE");
      }
    }
  }

  @Override
  public void open(OpenContext openContext) throws Exception {
    deviceCDCRow =
        getRuntimeContext().getState(new ValueStateDescriptor<>("Device CDC data", Row.class));
  }
}
