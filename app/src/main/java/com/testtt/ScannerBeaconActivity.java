package com.testtt;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.testtt.beacon.BeaconScanner;
import com.testtt.beacon.BeaconScannerFragment;


public class ScannerBeaconActivity extends AppCompatActivity  {


    private BeaconScannerFragment mBeaconScannerFragment = BeaconScannerFragment.newInstance();
    private static final int REQUEST_ENABLE_BT =1;
    private long mLastBackTime = 0;
    private long mCurrentBackTime  = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_beacon);


        // Ensure that Bluetooth exists
        if (!ensureBleExists())
            finish();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //this fragment is  prevented  to nullpointer
        getSupportFragmentManager().beginTransaction()
                .add(R.id.ly_frame, new android.support.v4.app.Fragment())
                .addToBackStack(null)
                .commit();

        setFragment();

        BeaconScanner.getInstance().onCreate(this);
        DirSensor.getInstance().onCreate(this);


    }
    @Override
    protected void onResume(){
        super.onResume();

        if (!isBleEnabled())
            enableBle();

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                } else
                    finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void setFragment(){

        mBeaconScannerFragment.setEnterTransition(new Explode());
        mBeaconScannerFragment.setExitTransition(new Explode());
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.ly_frame, mBeaconScannerFragment).commit();
    }
    /**
     * Checks whether the device supports Bluetooth Low Energy communication
     *
     * @return <code>true</code> if BLE is supported, <code>false</code> otherwise
     */
    private boolean ensureBleExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "no_ble", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    /**
     * Checks whether the Bluetooth adapter is enabled.
     */
    private boolean isBleEnabled() {
        final BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        final BluetoothAdapter ba = bm.getAdapter();
        return ba != null && ba.isEnabled();
    }

    /**
     * Tries to start Bluetooth adapter.
     */
    private void enableBle() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if(keyCode  == KeyEvent.KEYCODE_BACK && getSupportFragmentManager().getBackStackEntryCount() == 1){
            mCurrentBackTime = System.currentTimeMillis();
            if(mCurrentBackTime - mLastBackTime >= 2 *1000){
                FrameLayout layout = (FrameLayout)findViewById(R.id.ly_frame);
                Snackbar.make(layout,"在按一次退出",Snackbar.LENGTH_INDEFINITE).show();
                mLastBackTime = mCurrentBackTime;
            }else{
                finish();
            }
        }
        return super.onKeyDown(keyCode,event);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner_beacon, menu);
        return true;
    }

}
