package com.testtt.filter;

/**
 * Created by fish on 2015/5/13.
 */
/**
 * Interface that can be implemented to overwrite measurement and filtering
 * of Degree values
 */
public interface DegreeFilter {
    public void addMeasurement(float degree);
    public boolean noMeasurementsAcailable();
    public float caculateRssi();

}
