package com.example.diego.prova;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    ArrayList<StripesView> StripeViews;
    FrameLayout myFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myFrameLayout = (FrameLayout)findViewById(R.id.myFrameLayout);



        StripeViews = new ArrayList<StripesView>();
        //create images
        StripeViews.add(0,new StripesView(this.getApplicationContext()));
        FrameLayout.LayoutParams Lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT
        );
        Lp.setMargins(250, 50,4, 5);
        Lp.gravity = Gravity.LEFT;
        Lp.gravity = Gravity.TOP;
        myFrameLayout.addView(StripeViews.get(0),0,Lp);



        StripeViews.add(1,new StripesView(this.getApplicationContext()));
        Lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT
        );
        Lp.setMargins(0, 0,4, 5);
        Lp.gravity = Gravity.LEFT;
        Lp.gravity = Gravity.TOP;
        myFrameLayout.addView(StripeViews.get(1),1,Lp);


        //init touch
        mScaleDetector = new ScaleGestureDetector(this.getApplicationContext(), new ScaleListener());
        mTranslateMatrix.setTranslate(0, 0);
        mScaleMatrix.setScale(1, 1);

    }






    private static final int INVALID_POINTER_ID = 1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private float mScaleFactor = 1;
    private ScaleGestureDetector mScaleDetector;
    private Matrix mScaleMatrix = new Matrix();
    private Matrix mScaleMatrixInverse = new Matrix();

    private float mPosX;
    private float mPosY;
    private Matrix mTranslateMatrix = new Matrix();
    private Matrix mTranslateMatrixInverse = new Matrix();

    boolean FirstMove;
    private float mLastTouchX;
    private float mLastTouchY;

    private float[] mInvalidateWorkingArray = new float[6];
    private float[] mDispatchTouchEventWorkingArray = new float[2];
    private float[] mOnTouchEventWorkingArray = new float[2];

    private float mFocusY;
    private float mFocusX;


    private float[] scaledPointsToScreenPoints(float[] a) {
        mScaleMatrix.mapPoints(a);
        mTranslateMatrix.mapPoints(a);
        return a;
    }

    private float[] screenPointsToScaledPoints(float[] a){
        mTranslateMatrixInverse.mapPoints(a);
        mScaleMatrixInverse.mapPoints(a);
        return a;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mOnTouchEventWorkingArray[0] = ev.getX();
        mOnTouchEventWorkingArray[1] = ev.getY();

        mOnTouchEventWorkingArray = scaledPointsToScreenPoints(mOnTouchEventWorkingArray);

        ev.setLocation(mOnTouchEventWorkingArray[0], mOnTouchEventWorkingArray[1]);
        mScaleDetector.onTouchEvent(ev);

        // this is called if there is an ACTION DOWN inside a child, the onInterceptTouchEvent is true only for
        // sequent move event
        if (FirstMove) {
            mDispatchTouchEventWorkingArray[0] = ev.getX();
            mDispatchTouchEventWorkingArray[1] = ev.getY();
            mDispatchTouchEventWorkingArray = screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);

            mLastTouchX = ev.getX();
            mLastTouchY = ev.getY();
            // Save the ID of this pointer
            mActivePointerId = ev.getPointerId(0);
            FirstMove=false;
        }


        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;

                // Save the ID of this pointer
                mActivePointerId = ev.getPointerId(0);
                break;

            }

            case MotionEvent.ACTION_MOVE: {

                // Find the index of the active pointer and fetch its position
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;
                mTranslateMatrix.preTranslate(dx, dy);
                mTranslateMatrix.invert(mTranslateMatrixInverse);

                mLastTouchX = x;
                mLastTouchY = y;

                Iinvalidate();
                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    Log.i("ddd", "" + ev.getY(newPointerIndex));
                }
                break;
            }
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            if (detector.isInProgress()) {
                mFocusX = detector.getFocusX();
                mFocusY = detector.getFocusY();
            }
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            mScaleMatrix.setScale(mScaleFactor, mScaleFactor,
                    mFocusX, mFocusY);
            mScaleMatrix.invert(mScaleMatrixInverse);
            Iinvalidate();
            //requestLayout();


            return true;
        }
    }


    public void Iinvalidate(){
        Log.i("i","invalidate CALLED !!!!!!");
        Log.i("i","FattoreScala : " + mScaleFactor);
        Log.i("i","mFocusX : " + mFocusX);
        Log.i("i","mFocusY : " + mFocusY);
        //TranslateAnimation anim=new TranslateAnimation(0, 0, 0, 40);
        //anim.setFillAfter(true);
        //anim.setDuration(1000);
        //StripeViews.get(1).startAnimation(anim);



        TranslateAnimation anim = new TranslateAnimation(0, 100, 0, 100);
        anim.setDuration(1000);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)StripeViews.get(1).getLayoutParams();
                params.topMargin += 100;
                params.leftMargin += 100;
                StripeViews.get(1).setLayoutParams(params);
            }
        });

        StripeViews.get(1).startAnimation(anim);



    }








    public class StripesView extends View {


        public StripesView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public StripesView(Context context) {
            super(context);
        }

        public void init() {
            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            int desiredWidth = 200;
            int desiredHeight = 200;
            int width;
            int height;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int parentHeight = MeasureSpec.getSize(heightMeasureSpec);


            //Measure Width
            if (widthMode == MeasureSpec.EXACTLY) {
                //Must be this size
                width = parentWidth;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                //Can't be bigger than...
                width = Math.min(desiredWidth, parentWidth);
            } else {
                //Be whatever you want
                width = desiredWidth;
            }

            //Measure Height
            if (heightMode == MeasureSpec.EXACTLY) {
                //Must be this size
                height = parentHeight;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                //Can't be bigger than...
                height = Math.min(desiredHeight, parentHeight);
            } else {
                //Be whatever you want
                height = desiredHeight;
            }


            height = width;

            //MUST CALL THIS
            setMeasuredDimension(width, height);
        }


        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawColor(0x11880000);
        }

    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
