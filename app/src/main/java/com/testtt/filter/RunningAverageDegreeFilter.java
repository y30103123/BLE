package com.testtt.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by fish on 2015/5/13.
 */
public class RunningAverageDegreeFilter implements DegreeFilter {
    private static final String TAG = "RunningAverageDegreeFilter";
    public static long DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS =5000; /* 5 seconds */
    private static long sampleExpirationMilliseconds = DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS;
    private ArrayList<Measurement> mMeasurements = new ArrayList<Measurement>();

    @Override
    public void addMeasurement(float degree) {
        Measurement measurement = new Measurement();
        measurement.degree=degree;
        measurement.timestamp=new Date().getTime();
        mMeasurements.add(measurement);
    }

    @Override
    public boolean noMeasurementsAcailable() {
        return mMeasurements.size() == 0;
    }

    @Override
    public float caculateRssi() {
        refreshMeasurements();
        int size=mMeasurements.size();
        int startIndex=0;
        int endIndex=size-1;
        if(size>2){
            startIndex = size/10+1;
            endIndex = size-1;
        }

        float sum=0;
        for(int i=startIndex;i<=endIndex;i++)
            sum+=mMeasurements.get(i).degree;
        float runningAverage = sum/(endIndex-startIndex+1);

        return runningAverage;
    }
    public void clearOldData(){
        mMeasurements.clear();
    }
    private synchronized void refreshMeasurements(){
        Date now = new Date();
        ArrayList<Measurement> newMeasurements = new ArrayList<Measurement>();
        Iterator<Measurement> iterator = mMeasurements.iterator();
        while(iterator.hasNext()){
            Measurement measurement = iterator.next();
            if(now.getTime()-measurement.timestamp<sampleExpirationMilliseconds){
                newMeasurements.add(measurement);
            }
        }
        mMeasurements=newMeasurements;
        Collections.sort(mMeasurements);
    }

    private class Measurement implements Comparable<Measurement>{
        Float degree;
        long timestamp;
        @Override
        public int compareTo(Measurement arg0) {
            return degree.compareTo(arg0.degree);
        }
    }

    public static void setSampleExpirationMilliseconds(long newSampleExpirationMilliseconds) {
        sampleExpirationMilliseconds = newSampleExpirationMilliseconds;
    }

}
