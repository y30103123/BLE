package com.testtt;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TraceDegreeService extends Service {

    private long mStartTime;
    public static long DEFAULT_TRACE_MILLISECONDS = 3000; /* 5 seconds */
    public static long DEFAULT_TRACE_INTERVAL = 100; /* 0.1 seconds */
    private List<Float> mDegreeList;
    private float mInitDegree;
    private float mLeftDegree;

    public TraceDegreeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDegreeList = new ArrayList<>();
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Float InitDegree = setting.getFloat("DEGREE", 999);
        mInitDegree = InitDegree;
        startTrace();
        DirSensor.getInstance().onResume();
        Log.i("service", "onCreate");

    }

        public void startTrace() {
            mStartTime = new Date().getTime();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean timeisup = false;
                    long intervaltime=mStartTime;
                    int i=0;
                    while (!timeisup) {
                        Date date = new Date();
                        long time = date.getTime();
                        if (time - mStartTime <= DEFAULT_TRACE_MILLISECONDS) {//3秒後停止
                            if(time - intervaltime >= DEFAULT_TRACE_INTERVAL) {//每0,1秒紀錄一次
                                intervaltime = time;
                                mDegreeList.add(DirSensor.getInstance().getNowDegree());
                                Log.i("service", Float.toString(DirSensor.getInstance().getNowDegree()) + ", " + Integer.toString(i++));
                            }
                        } else {
                            timeisup = true;
                            mLeftDegree = caculateDegree();

                            SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor=setting.edit();
                            editor.putFloat("LEFT_DEGREE", mLeftDegree);
                            editor.commit();
                            Log.i("service", "left " + Float.toString(mLeftDegree));
                            DirSensor.getInstance().onPause();
                            stopSelf();
                        }
                    }
                }
            }).start();
        }

        private float caculateDegree() {
            int size = mDegreeList.size();
            int startIndex = 0;
            int endIndex = size - 1;
            if (size > 2) {
                startIndex = size / 2 + 1;
                endIndex = size - 1;
            }
            float sum = 0;
            for (int i = startIndex; i <= endIndex; i++)
                sum += mDegreeList.get(i);
            float leftdegree = sum / (endIndex - startIndex + 1);
            return leftdegree;
        }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("service", "onDestroy");
    }
}
