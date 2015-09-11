package com.testtt.beacon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.testtt.DirSensor;
import com.testtt.DrawView;
import com.testtt.R;
import com.testtt.TraceDegreeService;

/**
 * Created by fish on 2015/7/28.
 */
public class BeaconDisplayFragment extends Fragment {
    private View rootView;
    private static BeaconDisplayFragment mFragment;
    private AppCompatActivity mActivity;
    private RelativeLayout mLayout;
    private Button mStart;
    private ImageView mSetting;
    private DrawView mDrawView;
    private boolean serviceStart=true;
    //check clear init degree
    private int mMajor[];

    public BeaconDisplayFragment() {
        super();
    }

    public static BeaconDisplayFragment newInstance(int major[]) {
        if (mFragment == null){
            mFragment = new BeaconDisplayFragment();
        }
        Bundle args = new Bundle();
        args.putIntArray("MAJOR", major);
        if(mFragment.getArguments()!=null)
            mFragment.setArguments(null);
        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.getSupportActionBar().setTitle("Display");
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);


        findView();
        /////set transition to layout
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Float InitDegree = setting.getFloat("DEGREE", 999);
        if (InitDegree != 999) {//if it exise the init degree
            startDisplay(InitDegree);
        }
        Log.i("BeaconDisplay", "onActivityCreated");
    }

    private void findView() {
        mLayout = (RelativeLayout) rootView.findViewById(R.id.ly_relat);
        mDrawView = new DrawView(rootView.getContext());
        mStart = (Button) rootView.findViewById(R.id.btn_start);
        mStart.setOnClickListener(startListener);
        mSetting = (ImageView) rootView.findViewById(R.id.vw_image);

    }

    private void startDisplay(float degree) {
        mLayout.removeAllViews();
        mLayout.addView(mDrawView);
        Toast.makeText(getActivity(),"Beacon is located at " + Float.toString(degree) + " degree",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                return true;
            case R.id.action_clear:
                //clear init degree from memory
                SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = setting.edit();
                editor.clear().commit();
                mActivity.getSupportFragmentManager().popBackStack();
                serviceStart=false;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //get the major we choose
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMajor = getArguments().getIntArray("MAJOR");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_beacon_display, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) getActivity();

    }

    @Override
    public void onPause() {
        super.onPause();
        BeaconScanner.getInstance().onPause();
        DirSensor.getInstance().onPause();

        if(serviceStart) {
            Intent intent = new Intent(getActivity(), TraceDegreeService.class);
            getActivity().startService(intent);
        }
        serviceStart=true;
        Log.i("BeaconDisplay", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconScanner.getInstance().onResume();
        DirSensor.getInstance().onResume();
        Log.i("BeaconDisplay", "onResume");
    }

    private Button.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //set fade out animation
            Animation animation1 = new AlphaAnimation(1.0f, 0.0f);
            animation1.setDuration(500);
            animation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    float degree=DirSensor.getInstance().getNowDegree();
                    /////store the initDegree to memory
                    SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor=setting.edit();
                    editor.putFloat("DEGREE", degree);
                    editor.commit();
                    ////
                    startDisplay(degree);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mStart.startAnimation(animation1);
            mSetting.startAnimation(animation1);
        }
    };
}
