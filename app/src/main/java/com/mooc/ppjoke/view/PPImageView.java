package com.mooc.ppjoke.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mooc.libcommon.utils.PixUtils;
//import com.mooc.libcommon.utils.PixUtils;
//import com.mooc.libcommon.view.ViewHelper;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class PPImageView extends AppCompatImageView {

    public PPImageView(@NonNull Context context) {
        super(context);
    }

    public PPImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PPImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter(value = {"image_url","isCircle"})
    public static void setImageUrl(PPImageView view, String imageUrl, boolean isCircle){
        view.setImageUrl(view, imageUrl, isCircle, 0);
    }


    @BindingAdapter(value = {"image_url", "isCircle"},requireAll = true)
    public static void setImageUrl(PPImageView view, String imageUrl, boolean isCircle, int radius) {
        RequestBuilder<Drawable> builder = Glide.with(view).load(imageUrl);
        if (isCircle) {
            builder.transform(new CircleCrop());
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
                builder.override(layoutParams.width, layoutParams.height);
            }
            builder.into(view);
        }
    }

    @BindingAdapter(value ={"blur_url","radius"})
    public static void setBlurImageUrl(ImageView imageView,String blurUrl,int radius) {
        Glide.with(imageView).load(blurUrl).override(radius)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setBackground(resource);
                    }
                });
    }

    public void bindData(int widthPx,int heightPx,int marginLeft,String imageUrl){
        //给定默认的maxWidth, maxHeight
        bindData(widthPx,heightPx,marginLeft, PixUtils.getScreenWidth(),PixUtils.getScreenHeight(),imageUrl);
    }

    private void bindData(int widthPx, int heightPx, int marginLeft, final int maxWidth, final int maxHeight, String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)){
            setVisibility(GONE);
            return;
        }else {
            setVisibility(VISIBLE);
        }
        if (widthPx<=0||heightPx<=0){
            Glide.with(this).load(imageUrl).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    int height=resource.getIntrinsicHeight();
                    int width=resource.getIntrinsicWidth();
                    setSize(width,height,marginLeft,maxWidth,maxHeight);

                    //进行绑定
                    setImageDrawable(resource);
                }
            });
            return;
        }
        setSize(widthPx,heightPx,marginLeft,maxWidth,maxHeight);
        setImageUrl(this,imageUrl,false);
    }

    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth,finalHeight;
        if (width>height){
            finalWidth=maxWidth;
            //自适应
            finalHeight= (int) (height/(width*1.0f/finalWidth));
        }else {
            finalHeight=maxHeight;
            finalWidth= (int) (width/(height*1.0f/finalHeight));
        }

        ViewGroup.LayoutParams params=getLayoutParams();
        params.width=finalWidth;
        params.height=finalHeight;
        if (params instanceof FrameLayout.LayoutParams){
            ((FrameLayout.LayoutParams)params).leftMargin=height>width?PixUtils.dp2px(marginLeft):0;
        }else if (params instanceof LinearLayout.LayoutParams){
            ((LinearLayout.LayoutParams)params).leftMargin=height>width?PixUtils.dp2px(marginLeft):0;
        }
        setLayoutParams(params);
    }

    public void setImageUrl(String imageUrl) {
        setImageUrl(this,imageUrl,false);
    }
}
