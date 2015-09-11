package com.testtt.beacon;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testtt.R;
import com.testtt.database.BeaconContract;
import com.testtt.database.DatabaseHelper;

import java.util.LinkedList;

import no.nordicsemi.android.beacon.Beacon;
import no.nordicsemi.android.beacon.BeaconRegion;
import no.nordicsemi.android.beacon.Proximity;

/**
 * Created by fish on 2015/7/24.
 */
public class BeaconScannerFragment extends Fragment implements BeaconDetailFragment.DialogOnClickListener {


    private static BeaconScannerFragment mFragment;
    private View rootView;
    private static RecyclerView mRecyclerView;
    private BeaconAdapter mAdapter;
    private static DatabaseHelper mDatabaseHelper;
    private static LinkedList<Integer> mCheckBoxQueue;
    // Click this Button to start to fix position
    private FloatingActionButton mStar;
    //
    private BeaconDisplayFragment mDisplayFragment;
    private static Activity sActivity;
    private boolean mFirstSearch = true;
    private int mBeaconCount = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    public static BeaconScannerFragment newInstance() {
        if (mFragment == null)
            mFragment = new BeaconScannerFragment();
        return mFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        sActivity = getActivity();
        mCheckBoxQueue = new LinkedList<Integer>();
        mStar = (FloatingActionButton) rootView.findViewById(R.id.fab_start);
        mStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCheckBoxQueue.size() == 2) {
                    int major[] = new int[2];
                    //find the beacon major which we choose
                    for (int i = 0; i < 2; i++) {
                        int p = mCheckBoxQueue.pollFirst();//the firsit position
                        Cursor c = mDatabaseHelper.getAllRegions();
                        c.moveToPosition(p);
                        major[i] = Integer.parseInt(c.getString(3 /* MAJOR */));
                    }
                    mDisplayFragment = BeaconDisplayFragment.newInstance(major); //transmit major to display fragment
                    mDisplayFragment.setEnterTransition(new Fade());//Enter/Exit Animation
                    mDisplayFragment.setExitTransition(new Fade());
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.ly_frame, mDisplayFragment).commit();
                }
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.beacon_list);
        mDatabaseHelper = new DatabaseHelper(this.getActivity());
        Cursor cursor = mDatabaseHelper.getAllRegions();
        mAdapter = new BeaconAdapter(getActivity(), cursor);
        mAdapter.setClickListener(Listener, chklistener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setHasFixedSize(true);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(6 /* CHECK_BOX */) == 1)
                mCheckBoxQueue.add(i);
            cursor.moveToNext();
        }
    }

    public BeaconScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_beacon_scanner, container, false);
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        BeaconScanner.getInstance().onPause();
        BeaconScanner.getInstance().deleteListener(mListener);
        Log.i("ScannerFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconScanner.getInstance().onResume();
        BeaconScanner.getInstance().setUpdateListener(mListener);
    }

    private BeaconScanner.UpdateListener mListener = new BeaconScanner.UpdateListener() {

        @Override
        public void update(final Beacon[] beacons, BeaconRegion region) {
            ////Update list
            int length = beacons.length;
            if (length > 0) {
                for (Beacon beacon : beacons) {
                    final Cursor c = mDatabaseHelper.findRegionByBeacon(beacon);
                    int accuracy = (int) caculateProgress(beacon);
                    try {
                        if (c.moveToNext()) {
                            // Update beacon
                            c.getLong(0 /* _ID */);
                            mDatabaseHelper.updateRegionSignalStrength(c.getLong(0/*_ID*/), accuracy);
                            mDatabaseHelper.updateRegionRssi(c.getLong(0/*_ID*/), beacon.getRssi());
                        } else {
                            // Add new beacon
                            mDatabaseHelper.addRegion(beacon, "Beacon", BeaconContract.EVENT_GET_NEAR, BeaconContract.ACTION_MONA_LISA, null);
                        }
                    } finally {
                        c.close();
                    }
                }
                Cursor cursor = mDatabaseHelper.getAllRegions();
                mAdapter.swapCursor(cursor);
            } //update rssi strength

        }
    };

    //convert the value of Rssi to progress
    private float caculateProgress(Beacon beacon) {
        float accuracy = 5;
        if (Proximity.UNKNOWN != beacon.getProximity() && beacon.getAccuracy() < accuracy)
            accuracy = beacon.getAccuracy();
        accuracy = -20 * accuracy + 100;
        return accuracy;
    }

    private CheckBox.OnCheckedChangeListener chklistener = new CheckBox.OnCheckedChangeListener() {
        /*
        *  It is used to check the number of checkbox checked , at most two
        * */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Cursor cursor = mDatabaseHelper.getAllRegions();

            int position = (int) buttonView.getTag();
            if (isChecked) {

                mCheckBoxQueue.add(position);
                cursor.moveToPosition(position);
                mDatabaseHelper.updateRegionCheckBox(cursor.getLong(0/* ID*/), 1); // 0 is unchecked
                if (mCheckBoxQueue.size() > 2) {
                    int p = mCheckBoxQueue.pollFirst();//the firsit position
                    CheckBox c = (CheckBox) mRecyclerView.findViewHolderForAdapterPosition(p).itemView.findViewById(R.id.checkbox);
                    c.setChecked(!c.isChecked());
                    cursor.moveToPosition(p);
                    mDatabaseHelper.updateRegionCheckBox(cursor.getLong(0/* ID*/), 0); // 0 is unchecked
                }
            } else {
                for (int i = 0; i < mCheckBoxQueue.size(); i++)
                    if (position == mCheckBoxQueue.get(i)) {
                        mCheckBoxQueue.remove(i);
                        cursor.moveToPosition(position);
                        mDatabaseHelper.updateRegionCheckBox(cursor.getLong(0/* ID*/), 0); // 0 is unchecked
                    }
            }
        }
    };

    private static void showDialog(int position) {
        Cursor c = mDatabaseHelper.getAllRegions();
        c.moveToPosition(position);
        BeaconDetailFragment fragment = BeaconDetailFragment.newInstance(c);
        fragment.setTargetFragment(newInstance(), 0);
        fragment.show(((AppCompatActivity) sActivity).getSupportFragmentManager(), "detail");
    }

    private BeaconAdapter.onItemClickListener Listener = new BeaconAdapter.onItemClickListener() {
        @Override
        public void onClick(View view, int position) {
            showDialog(position);
        }
    };

    @Override
    public void onClick(String name, Cursor cursor) {

        mDatabaseHelper.updateRegionName(cursor.getLong(0), name);
        Cursor c = mDatabaseHelper.getAllRegions();
        mAdapter.swapCursor(c);
    }
}
