package com.gbksoft.countrycodepickerlib;

/**
 * interface to listen to failure events
 */
public interface FailureListener {
    /**
     * when country auto detection failed.
     */
    void onCountryAutoDetectionFailed();
}
