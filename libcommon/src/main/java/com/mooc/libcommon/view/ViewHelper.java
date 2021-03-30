package com.mooc.libcommon.view;

import android.annotation.TargetApi;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.mooc.libcommon.R;

public class ViewHelper {

    public static final int RADIUS_ALL=0;
    public static final int RADIUS_LEFT=1;
    public static final int RADIUS_TOP=2;
    public static final int RADIUS_RIGHT=3;
    public static final int RADIUS_BOTTOM=4;

    public static void setViewOutline(View view, AttributeSet attributes,int defStyleAttr,int defStyleRes){

        //创建一个TypeArray对象进行解析
        TypedArray array=view.getContext().obtainStyledAttributes(attributes, R.styleable.viewOutLineStrategy,defStyleAttr,defStyleRes);
        int radius=array.getDimensionPixelSize(R.styleable.viewOutLineStrategy_clip_radius,0);
        int hideSide=array.getInt(R.styleable.viewOutLineStrategy_clip_side,0);
        array.recycle();
        setViewOutline(view,radius,hideSide);
    }

    public static void setViewOutline(View owner, final int radius, final int radiusSide) {
        owner.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            @TargetApi(21)
            public void getOutline(View view, Outline outline) {
                int w=view.getWidth(),h=view.getHeight();
                if (w==0||h==0){
                    return;
                }
                //对边进行裁剪，当要裁剪左边时，可以给右边加上radius,这样就仅剩左边被裁剪，右边会被新添加的radius覆盖
                if (radiusSide!=RADIUS_ALL){
                    int left=0,top=0,right=w,bottom=h;
                    if (radiusSide==RADIUS_LEFT){
                        //如果裁剪的是左边，则需要把右边进行覆盖,同理可得下面也是一样的
                        right+=radius;
                    }else if (radiusSide==RADIUS_TOP){
                        bottom+=radius;
                    }else if (radiusSide==RADIUS_RIGHT){
                        left-=radius;
                    }else if (radiusSide==RADIUS_BOTTOM){
                        top-=radius;
                    }
                    outline.setRoundRect(left,top,right,bottom,radius);
                    return;
                }
                int top=0,bottom=h,left=0,right=w;
                if (radius<=0){
                    outline.setRect(left,top,right,bottom);
                }else {
                    outline.setRoundRect(left,top,right,bottom,radius);
                }
            }
        });
        owner.setClipToOutline(radius>0);
        owner.invalidate();
    }

}
