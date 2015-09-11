package com.testtt.beacon;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.testtt.MainActivity;
import com.testtt.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.beacon.Beacon;
import no.nordicsemi.android.beacon.BeaconRegion;
import no.nordicsemi.android.beacon.BeaconServiceConnection;
import no.nordicsemi.android.beacon.ServiceProxy;

/**
 * Created by fish on 2015/5/8.
 */
public class BeaconScanner {
    private static BeaconScanner mBeaconScanner;
    private static final String TAG = "BeaconScanner";
    private UUID mMyUuid = UUID.fromString("01122334-4556-6778-899A-ABBCCDDEEFF0");
    private UUID mAnyUuid = BeaconRegion.ANY_UUID;
    private Context mContext;
    private List<UpdateListener> mUpdateListenerList;

    public interface UpdateListener {
        void update(final Beacon[] beacons, final BeaconRegion region);
    }

    public void setUpdateListener(UpdateListener listener) {
        mUpdateListenerList.add(listener);
    }
    public void deleteListener(UpdateListener listener){
        for(int i=0;i<mUpdateListenerList.size();i++){
            if(mUpdateListenerList.get(i)==listener)
                mUpdateListenerList.remove(i);
        }
    }

    public static BeaconScanner getInstance() {
        if (mBeaconScanner == null)
            mBeaconScanner = new BeaconScanner();
        return mBeaconScanner;
    }

    public BeaconScanner() {
        mUpdateListenerList= new ArrayList<UpdateListener>();
    }

    public void onCreate(Context c) {
        mContext = c;
    }

    public void onPause() {
        // This intent will be launched when user press the notification
        final Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Create a pending intent
        final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Create and configure the notification
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext); // the notification icon (small icon) will be overwritten by the BeaconService.
        builder.setSmallIcon(R.drawable.stat_sys_place).setContentTitle("Beacon is in range!").setContentText("Click to open app.");
        builder.setAutoCancel(true).setOnlyAlertOnce(true).setContentIntent(pendingIntent);
        // Start monitoring for the region
        ServiceProxy.startMonitoringForRegion(mContext, mMyUuid, 65, 55, builder.build());

        mConnection.stopMonitoringForRegion(mRegionListener);
        mConnection.stopRangingBeaconsInRegion(mBeaconsListener);
        ServiceProxy.unbindService(mContext, mConnection);
        Log.i(TAG, "onPause");
    }

    public void onResume() {
        ServiceProxy.bindService(mContext, mConnection);
        ServiceProxy.stopMonitoringForRegion(mContext, mMyUuid);
        Log.i(TAG, "onResume");
    }

    private BeaconServiceConnection.RegionListener mRegionListener = new BeaconServiceConnection.RegionListener() {
        @Override
        public void onEnterRegion(final BeaconRegion region) {
            Log.i(TAG, "onEnterRegion: " + region);
        }

        @Override
        public void onExitRegion(final BeaconRegion region) {
            Log.i(TAG, "onExitRegion: " + region);
        }
    };

    private BeaconServiceConnection.BeaconsListener mBeaconsListener = new BeaconServiceConnection.BeaconsListener() {
        @Override
        public void onBeaconsInRegion(final Beacon[] beacons, final BeaconRegion region) {
            //Log.i(TAG, "onBeaconsInRegion Region: " + region);
            if (mUpdateListenerList != null) {
                for(UpdateListener listener :mUpdateListenerList )
                    listener.update(beacons,region);
            }
        }
    };

    private BeaconServiceConnection mConnection = new BeaconServiceConnection() {
        @Override
        public void onServiceConnected() {
            Log.v(TAG, "Service connected");
            /**/
            startMonitoringForRegion(mAnyUuid, mRegionListener);
            startRangingBeaconsInRegion(mMyUuid, mBeaconsListener);
            startRangingBeaconsInRegion(mMyUuid, 65, mBeaconsListener);
            startRangingBeaconsInRegion(mAnyUuid, mBeaconsListener);
        }

        @Override
        public void onServiceDisconnected() {
            Log.v(TAG, "Service disconnected");
        }
    };


}
