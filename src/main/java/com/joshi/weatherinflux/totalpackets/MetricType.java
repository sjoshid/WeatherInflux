package com.joshi.weatherinflux.totalpackets;

public enum MetricType {
  UNICAST(0x001),
  MULTICAST(0x010),
  BROADCAST(0x100);

  private final int value;

  private MetricType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
