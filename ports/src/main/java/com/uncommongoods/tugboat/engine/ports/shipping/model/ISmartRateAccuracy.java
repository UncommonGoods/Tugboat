package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for SmartRateAccuracy operations
 */
public interface ISmartRateAccuracy {
    /**
     * Get the percentile 50.
     *
     * @return the percentile 50
     */
    Integer getPercentile50();

    /**
     * Get the percentile 75.
     *
     * @return the percentile 75
     */
    Integer getPercentile75();

    /**
     * Get the percentile 85.
     *
     * @return the percentile 85
     */
    Integer getPercentile85();

    /**
     * Get the percentile 90.
     *
     * @return the percentile 90
     */
    Integer getPercentile90();

    /**
     * Get the percentile 95.
     *
     * @return the percentile 95
     */
    Integer getPercentile95();

    /**
     * Get the percentile 97.
     *
     * @return the percentile 97
     */
    Integer getPercentile97();

    /**
     * Get the percentile 99.
     *
     * @return the percentile 99
     */
    Integer getPercentile99();
}
