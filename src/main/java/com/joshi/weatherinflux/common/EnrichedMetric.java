package com.joshi.weatherinflux.common;

// Marker interface.
public interface EnrichedMetric<K> {
  Metric<K> getMetric();
}
