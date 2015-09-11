package com.testtt.filter;

/**
 * Created by AltBeacon on 2015/5/12.
 */
/**
 * Interface that can be implemented to overwrite measurement and filtering
 * of RSSI values
 */
public interface RssiFilter {
    public void addMeasurement(Integer rssi);
    public boolean noMeasurementsAcailable();
    public double caculateRssi();

}
