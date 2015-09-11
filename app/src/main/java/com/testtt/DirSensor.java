package com.testtt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;

import com.testtt.filter.RunningAverageDegreeFilter;

/**
 * Created by fish on 2015/5/7.
 */
public class DirSensor implements SensorEventListener {

    private float[] r = new float[9];
    private float[] values = new float[3];
    private float[] gravity = null;
    private float[] geomagnetic = null;
    private float[] gyrscope=null;
    ///移動參數
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    ////
    private SensorManager mSensorManager = null;
    private static DirSensor mInstance = new DirSensor();
    private RunningAverageDegreeFilter mAverageDegree;
    //

    private Context mContet;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://方位角
                    if (gravity != null && geomagnetic != null) {
                        if (SensorManager.getRotationMatrix(r, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(r, values);
                            float degree = (float) ((360f + values[0] * 180f / Math.PI) % 360);
                            mAverageDegree.addMeasurement(degree);
                        }
                    }
                    break;
                case 1:
                    float x = gravity[0];
                    float y = gravity[1];
                    float z = gravity[2];
                    mAccelLast = mAccelCurrent;
                    mAccelCurrent = FloatMath.sqrt(x * x + y * y + z * z);
                    float delta = mAccelCurrent - mAccelLast;
                    mAccel = mAccel * 0.9f + delta;
                    break;

            }
        }
    };


    public void onCreate(Context context) {
        mContet=context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAverageDegree = new RunningAverageDegreeFilter();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度
                gravity = event.values;
                handler.sendEmptyMessage(0);
                handler.sendEmptyMessage(1);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD://磁場
                geomagnetic = event.values;
                handler.sendEmptyMessage(0);
                break;


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void onResume() {
        //註冊加速度感應器
        Sensor acceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, acceleSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //註冊磁場
        Sensor magSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }


    public void onPause() {
        if (mSensorManager == null) {
            return;
        }
        mSensorManager.unregisterListener(this);
    }

    public static DirSensor getInstance() {
        return mInstance;
    }

    public void clearOldData() {
        mAverageDegree.clearOldData();
    }

    public float getAverageDegree() {
        return mAverageDegree.caculateRssi();
    }

    public float getNowDegree() {
        return (float) ((360f + values[0] * 180f / Math.PI) % 360);
    }

    public boolean getMoving() {
        return mAccel > 0.3;
    }


}
