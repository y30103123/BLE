package com.testtt.beacon;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testtt.R;
import com.testtt.database.CursorRecyclerViewAdapter;


/**
 * Created by skyfishjy on 10/31/14.
 */
public class BeaconAdapter extends CursorRecyclerViewAdapter<BeaconAdapter.ViewHolder> {

    public interface onItemClickListener {
        void onClick(View view, int position);
    }
    private onItemClickListener mClickListener;
    private CheckBox.OnCheckedChangeListener mCheckBoxListener;
    public void setClickListener(onItemClickListener listener,CheckBox.OnCheckedChangeListener clistener) {
        this.mClickListener = listener;
        this.mCheckBoxListener = clistener;
    }
    public BeaconAdapter(Context context,Cursor cursor){
        super(context,cursor);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener  {

        private onItemClickListener mClickListener;

        public void setClickListener(onItemClickListener listener) {
            this.mClickListener = listener;
        }
        private TextView mName;
        private TextView mRssi;
        private ProgressBar mProgressBar;
        private CheckBox mCheckBox;
        public ViewHolder(View v) {
            super(v);
            mName = getView(R.id.tx_name, v);
            mProgressBar = getView(R.id.progress, v);
            mRssi = getView(R.id.tx_rssi, v);
            mCheckBox = getView(R.id.checkbox, v);
            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mClickListener.onClick(v, getAdapterPosition());
        }
        private <E extends View> E getView(int id, View v) {
            return (E) v.findViewById(id);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_beacon_list_item, viewGroup, false);
        return new ViewHolder(v);
    }
    private boolean checked(int c){
        if(c==0)
            return false;
        else
            return true;
    }
    @Override
    public void onBindViewHolder( ViewHolder viewHolder, Cursor cursor) {
        viewHolder.mName.setText(cursor.getString(1 /* NAME */));
        viewHolder.mRssi.setText("Rssi : "+ cursor.getInt(7 /* RSSI */) + "dbm");
        viewHolder.mProgressBar.setProgress(cursor.getInt(5 /* SIGNAL_STRENGTH */));
        viewHolder.mCheckBox.setChecked(checked(cursor.getInt(6 /* CHECK_BOX */)));
        viewHolder.mCheckBox.setOnCheckedChangeListener(mCheckBoxListener);
        viewHolder.mCheckBox.setTag(cursor.getPosition());
        viewHolder.setClickListener(mClickListener);

    }
}