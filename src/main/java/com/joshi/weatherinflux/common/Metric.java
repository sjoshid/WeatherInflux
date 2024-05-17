package com.joshi.weatherinflux.common;

// Maker interface
public interface Metric<K> {
  long getTimestamp();

  K getKey();
}
