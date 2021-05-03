package com.mooc.ppjoke.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.mooc.libcommon.utils.PixUtils;
import com.mooc.ppjoke.R;

public class RecordView extends View implements View.OnLongClickListener,View.OnClickListener {
    private final int radius;
    private final int progressWidth;
    private final int progressColor;
    private final int fillColor;
    private final int maxDuration;
    private int progressMaxValue;
    private static final int PROGRESS_INTERVAL=100;
    private boolean isRecording;
    private final Paint fillPaint;
    private int progressValue;
    private final Paint progressPaint;
    private onRecordListener mListener;

    public RecordView(Context context) {
        super(context);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.RecordView,defStyleAttr,defStyleRes);
        radius=typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius,0);
        progressWidth=typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, PixUtils.dp2px(3));
        progressColor=typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        fillColor=typedArray.getColor(R.styleable.RecordView_progress_color,Color.WHITE);
        maxDuration=typedArray.getInteger(R.styleable.RecordView_duration,10);
        setMaxDuration(maxDuration);
        typedArray.recycle();

        //创建一个画笔Paint对象
        fillPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        progressPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getWidth();
        int height=getHeight();
        if (isRecording){
            canvas.drawCircle(width/2,height/2,width/2,fillPaint);

            int left=progressWidth/2;
            int top=progressWidth/2;
            int right=width-progressWidth/2;
            int bottom=height-progressWidth/2;
            float sweepAngle=(progressValue*1.0f/progressMaxValue)*360;
            canvas.drawArc(left,top,right,bottom,-90,sweepAngle,false,progressPaint);
        }else {
            canvas.drawCircle(width/2,height/2,radius,fillPaint);
        }
    }

    private void setMaxDuration(int maxDuration) {
        this.progressMaxValue=maxDuration*1000/PROGRESS_INTERVAL;
    }

    public void setOnRecordListener(onRecordListener listener){
        mListener=listener;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    public interface onRecordListener {
        void onClick();
        void onLongClick();
        void onFinish();
    }
}
