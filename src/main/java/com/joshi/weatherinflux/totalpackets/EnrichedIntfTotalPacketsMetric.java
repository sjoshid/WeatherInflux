package com.joshi.weatherinflux.totalpackets;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrichedIntfTotalPacketsMetric {
  private final String id;
  private String deviceId;
  private String acna;
  private String sponsoredBy;
  private final long timestamp;
  private final float inTotalPackets;
  private final float outTotalPackets;
}
