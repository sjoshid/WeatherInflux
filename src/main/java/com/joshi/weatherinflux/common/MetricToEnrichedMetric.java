package com.joshi.weatherinflux.common;

/**
 * Take a metric and a CDC row (which has all inv. details) and returns and enriched metric.
 *
 * @param <I> Raw metric (mostly from Kafka)
 * @param <O> CDC row (with inv. details)
 * @param <E> Enriched metric.
 */
public interface MetricToEnrichedMetric<I, O, E> {
  E giveE(I value, O cdcRow);
}
