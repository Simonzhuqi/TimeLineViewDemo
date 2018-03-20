package com.ikonke.timelineviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ZHUQI on 2018/3/19.
 */

public class TimeLineView extends View {
    private static final String TAG = "TimeLineView";
    private static final long LONG_PRESS_TIME = 2000;
    private int measureWidth;
    private int measureHeight;
    /**
     * 背景画笔
     */
    private Paint backgroundPaint;
    /**
     * 刻度画笔
     */
    private Paint scalePaint;
    /**
     * 当前刻度标识画笔
     */
    private Paint redFlagPaint;

    private float currentPosition;
    private float startX;
    private boolean isSelectingTime;
    private Date date;
    private long currentTime;
    private float selectPosition;

    private OnTimeChangeListener mOnTimeChangeListener;
    private OnTimeSelectedListener mOnTimeSelectedListener;
    private long selectTime;
    private long startTime;
    private float startY;
    private Paint textPaint;
    private String currentTimeString;
    private boolean isPress;
    private boolean isPlaying;

    public TimeLineView(Context context) {
        this(context,null);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        this.mOnTimeChangeListener = onTimeChangeListener;
    }

    public void setOnTimeSelectedListener(OnTimeSelectedListener mOnTimeSelectedListener) {
        this.mOnTimeSelectedListener = mOnTimeSelectedListener;
    }

    private void initView() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#77333333"));
        scalePaint = new Paint();
        scalePaint.setColor(Color.WHITE);
        redFlagPaint = new Paint();
        redFlagPaint.setColor(Color.RED);
        currentPosition = measureHeight/2;
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);
        currentTime = System.currentTimeMillis();
        date = new Date();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureWidth = measureWidth(widthMeasureSpec);
        measureHeight = measureHeight(heightMeasureSpec);
        startY = measureHeight/3;
        // 设置自定义的控件的大小
        setMeasuredDimension(measureWidth, measureHeight);

    }
    private int measureWidth(int pWidthMeasureSpec) {
        int result = 0;
        int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);
        int widthSize = MeasureSpec.getSize(pWidthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = widthSize;
                break;
        }
        return result;
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;

        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawScale(canvas);
        drawRedFlag(canvas);
        drawTimeText(canvas);
    }

    private void drawTimeText(Canvas canvas) {
        if(true && currentTimeString != null){
            canvas.drawText(currentTimeString, (float) (currentPosition-50*2.5/2),startY/2,textPaint);
        }
    }

    private void drawRedFlag(Canvas canvas) {
        canvas.drawLine(currentPosition,startY,currentPosition,measureHeight,redFlagPaint);
    }


    private void drawScale(Canvas canvas) {
        float startX = 0;
        float stopY = measureHeight;
        int totalSpan = getTotalSpan(isSelectingTime);
        for(int i = 0; i < totalSpan; i++){
            int startY = (int) ((measureHeight-this.startY)/3*2+this.startY);
            if(i%5 == 0){
                startY = (int) ((measureHeight-this.startY)/2+this.startY);
                if(i%10 == 0){
                    startY = (int) ((measureHeight-this.startY)/3+this.startY);
                }
            }
            canvas.drawLine(startX,startY,startX,stopY, scalePaint);
            startX += measureWidth*10f/totalSpan/10f;
        }
    }

    /**
     * 刻度数量
     * @param isSelectingTime
     * @return
     */
    private int getTotalSpan(boolean isSelectingTime) {
        return isSelectingTime ? 60 : 120;
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(0,startY,measureWidth,measureHeight,backgroundPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentPosition = (int) event.getX();
        currentTime = transPositionToTime(currentPosition);
        currentTimeString = getTimeFormatString(currentTime);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isPress = true;
                startX = event.getX();
                Log.d(TAG,"onTouchEvent MotionEvent = ACTION_DOWN event.getX() = "+currentPosition);
                mOnTimeChangeListener.onTimeChange(currentTime,currentTimeString);
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if(isLongPressed(startX,event.getX(),event.getDownTime(),event.getEventTime(),LONG_PRESS_TIME)){
                    Log.d(TAG,"onTouchEvent MotionEvent = LONG_PRESS event.getX() = "+currentPosition);
                    if(!isSelectingTime){
                        startTime = currentTime - 30*60*1000;
                        isSelectingTime = true;
                    }
                    mOnTimeChangeListener.onTimeChange( transPositionToTime(currentPosition),currentTimeString);
                }else {
                    mOnTimeChangeListener.onTimeChange(currentTime,currentTimeString);
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                isPress = false;
                if(isSelectingTime){
                    isSelectingTime = false;
                    //手指放开时，切换到原来24小时显示模式 开始播放
                    if(mOnTimeSelectedListener != null){
                        mOnTimeSelectedListener.onTimeSelected(currentTime);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * * 判断是否有长按动作发生 * @param lastX 按下时X坐标 * @param lastY 按下时Y坐标 *
     *
     * @param thisX
     *            移动时X坐标 *
     * @param lastDownTime
     *            按下时间 *
     * @param thisEventTime
     *            移动时间 *
     * @param longPressTime
     *            判断长按时间的阀值
     */
    private boolean isLongPressed(float lastX, float thisX, long lastDownTime, long thisEventTime,
                                  long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 50 && intervalTime >= longPressTime) {
            return true;
        }
        return false;
    }

    private long transPositionToTime(float position){
        long startTime = dayMilliSeconds(date);
        long playedTime = (long) (24*60*60*1000f * position/measureWidth);
        if(isSelectingTime){
            startTime = this.startTime;
            playedTime = (long) (60*60*1000f * position/measureWidth);
        }
        return startTime + playedTime;
    }

    private int transTimeToPosition(long time){
        long playedTime = time - dayMilliSeconds(date);
        return (int) (playedTime * measureWidth / 24*60*60*1000f);
    }

    private String getTimeFormatString(long time){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    /**
     * 获取指定日期 00:00的时间戳
     * @param date
     * @return
     */
    private long dayMilliSeconds(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }


    interface OnTimeChangeListener{
        void onTimeChange(long time, String timeString);
    }


    interface OnTimeSelectedListener{
        void onTimeSelected(long time);
    }

}

