package com.testtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.testtt.beacon.BeaconScanner;
import com.testtt.mathFormula.Formula;

import java.io.FileOutputStream;

import no.nordicsemi.android.beacon.Beacon;
import no.nordicsemi.android.beacon.BeaconRegion;

/**
 * Created by fish on 2015/4/14.
 */
public class DrawView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Paint paint;
    private Context mContex;
    private SurfaceHolder holder;
    private Thread mThread;
    private PointF mBeaconPoint = new PointF();
    private final static float unit = 5;
    private final static float unitPixel = 170;//1公尺長度為173.20508 像素 if unit is 10
    private float mLengthPixel = unitPixel * (unit / 10);
    private BeaconScanner mBeaconScanner;
    private Formula mFormula;
    private Bitmap mCompass;
    private float mInitDegree=0;

    public DrawView(Context context) {
        super(context);
        this.mContex = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        init();
        Log.i("DrawView", "con");
    }



    private BeaconScanner.UpdateListener mListener = new BeaconScanner.UpdateListener(){

        @Override
        public void update(Beacon[] beacons, BeaconRegion region) {
            mFormula.caculateCoordinate(beacons);
        }
    };
    private void init() {
        mFormula =new Formula(mContex);
        mBeaconScanner = BeaconScanner.getInstance();
        Resources resource = getResources();


        paint = new Paint(); //
        paint.setColor(Color.RED);
        holder = getHolder();
        holder.addCallback(this);
    }


    private void setInitDegree() {
        mFormula.setInitDegree();
    }

    private void update() {

        mBeaconPoint.set(mFormula.getX(),mFormula.getY());
        this.mInitDegree = mFormula.getInitDegree();
       // Log.i("DrawView", Float.toString(mBeaconX) + "," + Float.toString(mBeaconY));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("DrawView", "create");
        mBeaconScanner.setUpdateListener(mListener);
        setInitDegree();
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread = null;
        mBeaconScanner.deleteListener(mListener);
        Log.i("DrawView", "destroy");
    }


    @Override
    public void run() {
        Canvas canvas = null;
        while (mThread != null) {
            try {
                //
                canvas = holder.lockCanvas();
                clearDraw(canvas);
                update();
                Draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                //
                mThread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void Draw(Canvas canvas) {

        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        //compass
//        int width = mCompass.getWidth();
//        int height = mCompass.getHeight();
        float rotate = DirSensor.getInstance().getNowDegree();
//        Matrix matrix = new Matrix();
//        matrix.postScale(3 / 5f, 3 / 5f);
//        Bitmap bitmap = Bitmap.createBitmap(mCompass,0,0,width,height,matrix,true);
//        width = bitmap.getWidth()/2;
//        height = bitmap.getHeight() / 2;
//        int X = getMeasuredWidth()-width*2;
//        int Y = getMeasuredHeight()-height*2;
//        matrix.postTranslate(-width, -height);
//        matrix.postRotate(-rotate);
//        matrix.postTranslate(X + width, Y + height);
//        canvas.drawBitmap(mCompass, matrix, paint);

        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);

        int X = -getMeasuredWidth() / 2;
        int Y =  getMeasuredHeight() / 2;
        paint.setColor(Color.WHITE);
        paint.setTextSize(50f);
        canvas.drawText(String.format("%.1f", rotate), X + getMeasuredWidth() / 5, Y - paint.getTextSize(), paint);
        canvas.drawText(String.format("%.1f", mInitDegree), X + getMeasuredWidth() / 5, Y - 2 * paint.getTextSize(), paint);
        paint.setColor(Color.GREEN);
        canvas.drawText("Az: ", X, Y - paint.getTextSize(), paint);
        canvas.drawText("Beacon :", X, Y - 2 * paint.getTextSize(), paint);

        //circle

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.parseColor("#00FF00"));

        int interval = 2 ;
        for(int i = 1 ;i < 4; i++){
            canvas.drawCircle(0, 0, mLengthPixel * i * interval, paint);
            canvas.drawText(Integer.toString(i * interval) + " 公尺", 0, (mLengthPixel * i * interval) * -1 + paint.getTextSize(), paint);
        }
        float y = mLengthPixel * 3 * interval * -1;

        float rx =(float) (y * Math.sin(rotate * Math.PI /180));
        float ry = (float) (y * Math.cos(rotate *  Math.PI /180));
        canvas.drawLine(0, 0, rx, ry, paint);
        canvas.drawText("N", rx, ry, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(30);
        paint.setColor(Color.BLUE);
        canvas.drawPoint(0, 0, paint);
        paint.setColor(Color.RED);
        canvas.drawPoint(mBeaconPoint.x * unit, mBeaconPoint.y * unit, paint);

    }

    private void clearDraw(Canvas canvas) {

        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        //清屏
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));


    }
}