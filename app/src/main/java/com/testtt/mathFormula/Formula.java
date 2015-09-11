package com.testtt.mathFormula;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.preference.PreferenceManager;
import android.util.Log;

import com.testtt.DirSensor;
import com.testtt.filter.KalmanFilter;
import com.testtt.filter.RssiFilter;
import com.testtt.filter.RunningAverageDegreeFilter;
import com.testtt.filter.RunningAverageRssiFilter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import no.nordicsemi.android.beacon.Beacon;
import no.nordicsemi.android.beacon.Proximity;

/**
 * Created by fish on 2015/7/27.
 */
public class Formula {
    private static final String TAG = "Formula";
    private Beacon mBeacon[];
    private RssiFilter mRssiFilter[];
    private float x0;
    private float y0;
    private float x1;
    private float y1;
    private float mMovingCounter = 0;//紀錄移動次數
    private float mLastDegree;
    private float mLastH;
    private boolean mRelativePosition = true; //the beacon is in front of me
    private boolean mOldRelativePosition = true;
    private float mInitDegree = 0;//對於beacon的初始角度
    private float mStartDegree = 0;//對於剛開程式時的角度
    private boolean mFirst = true;
    private float mLastD[] = {0, 0};
    ///
    private static int sPR0[] = {-69, -67};//beacon 距離1公尺的RSSI  nrf -70  taobo -70
    private Context mContex;

    public Formula(Context c) {
        mContex = c;
        mRssiFilter = new KalmanFilter[2];
        for (int i = 0; i < 2; i++) {
            mRssiFilter[i] = new KalmanFilter();
        }
    }

    public void caculateCoordinate(Beacon[] beacons) {
        mBeacon = beacons;
        float distance[] = {0, 0};
        setBeacon(distance);
        caculateCoordinate(distance);
    }

    public float getX() {
        return ((x0 + x1)) / 2;
    }

    public float getY() {
        return ((y0 + y1)) / 2;
    }

    private void caculateCoordinate(float []distance) {


        y0 = y1 = 0;
        x0 = -10;
        x1 = 10;
        float x3 = 0;
        float y3 = 0;
        float d = 1;
        float a, h;
        float x2, y2;
        float r0, r1;
        r0 = distance[0];
        r1 = distance[1];
        if (mFirst) {//第一次執行
            mLastD[0] = r0;
            mLastD[1] = r1;
        }
        if (r0 > r1) {// a
            if (r0 > d) {// ---x0----x1-----x2
                a = (r0 * r0 - r1 * r1 - d * d) / (2 * d);
                h = (float) (Math.sqrt(r0 * r0 - (a + d) * (a + d)));
                x2 = (x1 * (d + a) - (a * x0)) / d;
                y2 = (y1 * (d + a) - (a * y0)) / d;
            } else {// ---x0----x2-----x1
                a = (r0 * r0 - r1 * r1 + d * d) / (2 * d);
                h = (float) (Math.sqrt(r0 * r0 - a * a));
                x2 = x0 + a * (x1 - x0) / d;
                y2 = y0 + a * (y1 - y0) / d;
            }
        } else {// b
            if (r1 > d) {
                a = (r1 * r1 - r0 * r0 - d * d) / (2 * d);
                h = (float) (Math.sqrt(r1 * r1 - (a + d) * (a + d)));
                x2 = (x0 * (d + a) - (a * x1)) / d;
                y2 = (y0 * (d + a) - (a * y1)) / d;
            } else {
                a = (r1 * r1 - r0 * r0 + d * d) / (2 * d);
                h = (float) (Math.sqrt(r1 * r1 - a * a));
                x2 = x0 + a * (x1 - x0) / d;
                y2 = y0 + a * (y1 - y0) / d;
            }
        }
        if (Float.isNaN(h)) {
            h = mLastH;
        }
        if (mFirst) {//第一次執行
            if (mLastDegree != 999)
                checkFirstDirection();
            mFirst = false;
        } else {
            checkApproach(distance);
        }
        if (mRelativePosition) {
            Log.i(TAG, "case A:上");

            x3 = x2 - h * (y1 - y0) / d;
            y3 = y2 + h * (x1 - x0) / d;
        } else {
            Log.i(TAG, "case B:下");
            x3 = x2 + h * (y1 - y0) / d;
            y3 = y2 - h * (x1 - x0) / d;
        }
        mLastH = h;
        mLastDegree = DirSensor.getInstance().getAverageDegree();
        x0 -= x3;
        y0 -= y3;
        x1 -= x3;
        y1 -= y3;
        //Log.i(TAG, "x= "+Float.toString(((x0 + x1) / 2)*10) + " , y= " + Float.toString(((y0 + y1) / 2)*10));
    }

    private boolean range(float pivot, float nowdegree) {
        //180度range
        float lowdegree = pivot - 90;
        float highdegree = pivot + 90;
        if (lowdegree < 0) {
            if (nowdegree >= 270 && nowdegree <= 360)
                nowdegree -= 360;
        }
        return (nowdegree >= lowdegree && nowdegree <= highdegree);
    }

    private void checkFirstDirection() {
        mLastDegree = (mLastDegree >= 180) ? mLastDegree - 180 : mLastDegree + 180;
        //反方向  超過180轉向會超過360,所以要-360度
        mRelativePosition = range(mLastDegree, mStartDegree);
        mInitDegree = mLastDegree;
    }


    private void checkApproach(float []distance) {

        float nowDegree = DirSensor.getInstance().getNowDegree();
        boolean stand = false;


        if (isMoving()) {
            mMovingCounter++;
            if (mMovingCounter >= 2) {
                boolean newPosition = isLonger(distance);
                if (newPosition == mOldRelativePosition) {
                    if (mRelativePosition != newPosition) {
                        mInitDegree = (mInitDegree >= 180) ? mInitDegree - 180 : mInitDegree + 180;
                    }
                } else {
                    mOldRelativePosition = newPosition;
                    distance[0] = mLastD[0];
                    distance[1] = mLastD[1];
                }
                mMovingCounter = 0;
            }
        } else {
            mMovingCounter = 0;
            stand = true;
        }
        mRelativePosition = range(mInitDegree, nowDegree);
        if (!stand) {
            mLastD[0] = distance[0];
            mLastD[1] = distance[1];
        }

    }

    //true is that face to beacon
    private boolean isLonger(float []distance) {
        return mLastD[0] + mLastD[1] >= distance[0] + distance[1];
    }

    private boolean isMoving() {
        return DirSensor.getInstance().getMoving();
    }

    private void setBeacon(float []distance) {
        int i = 0;
        int rssi[] = {-100, -100};
        for (final Beacon beacon : mBeacon) {
            rssi[i] = beacon.getRssi();
            if (i == 1) {
                if (Math.abs(rssi[0] - rssi[1]) > 10) {
                    if (rssi[0] > rssi[1]) {
                        rssi[0] -= 5;
                        rssi[1] += 5;
                    } else {
                        rssi[0] += 5;
                        rssi[1] -= 5;
                    }
                }
            }
            i++;
        }
        for (int index = 0; index < 2; index++) {

            mRssiFilter[index].addMeasurement(rssi[index]);
            int calrssi = (int) mRssiFilter[index].caculateRssi();
            distance[index] = (float) calculateAccuracy(sPR0[index], calrssi);
            Log.i(TAG, Float.toString(distance[index]) + " , " + Integer.toString(calrssi) + ", " + Integer.toString(rssi[index]));
        }
    }

    protected static double calculateAccuracy(int txPower, int rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            return (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }
    }

    public void setInitDegree() {
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContex);
        Float InitDegree = setting.getFloat("DEGREE", 999);
        Float leftDegree = setting.getFloat("LEFT_DEGREE", 999);
        mInitDegree = InitDegree;
        mLastDegree = leftDegree;
        mStartDegree = DirSensor.getInstance().getNowDegree();
    }

    public float getInitDegree() {
        return mInitDegree;
    }
}
