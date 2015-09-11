package com.testtt.beacon;


import android.app.Dialog;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.testtt.R;
import com.testtt.database.DatabaseHelper;

/**
 * Created by FishMan on 2015/8/5.
 */
public class BeaconDetailFragment extends DialogFragment {

    private TextView mUUID;
    private TextView mMajor;
    private TextView mMinor;
    private EditText mName;
    private static Cursor mCursor;
    private static BeaconDetailFragment mFragment;
    private DialogOnClickListener callback;
    public interface DialogOnClickListener{
        void onClick(String name,Cursor cursor);
    }
    public BeaconDetailFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            callback = (DialogOnClickListener)getTargetFragment();
        }catch (ClassCastException e){
            throw new ClassCastException("must implement DialogOnClickListener");
        }
    }

    public static BeaconDetailFragment newInstance(Cursor cursor){
     if(mFragment==null){
         mFragment = new BeaconDetailFragment();
     }
        mCursor=cursor;
        return mFragment;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_beacon_detail,null);
        initView(view);
        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onClick(mName.getText().toString(),mCursor);
                    }
                }).setNegativeButton("Cancel", null);
        return builder.create();
    }

    private void initView(View v){
        mMajor=(TextView)v.findViewById(R.id.tv_major);
        mMinor=(TextView)v.findViewById(R.id.tv_minor);
        mUUID=(TextView)v.findViewById(R.id.tv_uuid);
        mName=(EditText)v.findViewById(R.id.edit_name);
        mMajor.setText(mCursor.getString(3 /* MAJOR */));
        mMinor.setText(mCursor.getString(4 /* MINOR */));
        mUUID.setText(mCursor.getString(2 /* UUID */));
        mName.setText(mCursor.getString(1 /* NAME */));
    }
}
