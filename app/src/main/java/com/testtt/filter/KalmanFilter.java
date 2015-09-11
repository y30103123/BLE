package com.testtt.filter;
/**
 * This class KalmanFilter aims to recursively provide better
 * and better estimates of the position of the watch. The construction of the Kalman filter is based on the
 * explaination <a href="http://greg.czerniak.info/guides/kalman1/"</a>.
 * @version 1.0 22 Nov 2013
 * @author Yentran Tran
 *
 */
/**
 * Created by fish on 2015/8/12.
 */
public class KalmanFilter implements  RssiFilter {
    /**
     * SensorNoise_DEFAUL is the estimated measurement error covariance as default value
     */
    private static final double SENSORNOISE_DEFAULT = 0.8;
    /**
     * Processnoise_DEFAULT is the estimated process error covariance as default value
     */
    private static final double PROCESSNOISE_DEFAULT = 0.125;
    /**.
     * priorErrorVariance is the prior ErrorVariance which we get from the variable errorCovariance
     * errorCovarRSSI is the newest estimate of the average error for each part of the state.
     * kalmanGain
     */
    private double priorErrorVariance,kalmanGain;

    /**
     * sensorNose is the estimated measurement error covariance
     * processNoise is the estimated process error covariance
     */
    private static double sensorNoise, processNoise;

    public KalmanFilter() {
        super();
        processNoise =PROCESSNOISE_DEFAULT;
       sensorNoise = SENSORNOISE_DEFAULT;
    }

    /**
     * priorRSSI is the previous calculated RSSI value.
     * estimateRSSI is the new calculate RSSI value
     * rawRSSI is temporary parameter to store the estimateRSSI
     */
    private double priorRSSI, estimateRSSI, rawRSSI;

    /**
     * prioErrorCovarRSSI is the previous Covariance
     * errorCovarRSSI is the new calculated Covariance
     */
    private double priorErrorCovarRSSI,errorCovarRSSI;
    /**
     * firstTimeRun check if the Filter is used first time
     */
    private static boolean firstTimeRun = true;


    /**
     * @param rssi is the RSSI value we want to estimate better
     * @return A new RSSI value which is calculated with the kalman filter based on the previous RSSI value
     */

    private double mNewRssi;
    @Override
    public void addMeasurement(Integer rssi) {
        mNewRssi=rssi;
        if (firstTimeRun) {
            priorRSSI = mNewRssi;
            priorErrorCovarRSSI = 0.5;
            firstTimeRun = false;
        }

        //Prediction Part

        else {
            priorRSSI = estimateRSSI;
            priorErrorCovarRSSI = errorCovarRSSI + processNoise;
        }
    }

    @Override
    public boolean noMeasurementsAcailable() {
        return false;
    }
    /**
     *
     * @return A new RSSI value which is calculated with the kalman filter based on the previous RSSI value
     */
    @Override
    public double caculateRssi() {
        //Correction Part

        rawRSSI = mNewRssi;
        kalmanGain = priorErrorCovarRSSI / (priorErrorCovarRSSI + sensorNoise);
        estimateRSSI = priorRSSI + (kalmanGain * (rawRSSI - priorRSSI));
        errorCovarRSSI = (1 - kalmanGain) * priorErrorCovarRSSI;

        mNewRssi = estimateRSSI;

        return mNewRssi;
    }


}
