package com.mooc.ppjoke.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mooc.libcommon.utils.PixUtils;
import com.mooc.ppjoke.R;

/**
 * 列表视频播放专用
 */
public class ListPlayerView extends FrameLayout {

    public View bufferVeiw;
    public PPImageView cover,blur;
    protected ImageView playBtn;



    private String mCategory;
    private int mWidthPx;
    private int mHeightPx;
    private String mVideoUrl;

    public ListPlayerView(@NonNull Context context) {
        super(context);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view,this,true);

    }

    public void bindData(String category,int widthPx,int heightPx,String coverUrl,String videoUrl){

        mCategory = category;
        mWidthPx = widthPx;
        mHeightPx = heightPx;
        mVideoUrl = videoUrl;
        cover.setImageUrl(coverUrl);
        //如果该视频的宽度小于高度,则高斯模糊背景图显示出来
        if (widthPx<heightPx){
            PPImageView.setBlurImageUrl(blur,coverUrl,10);
            blur.setVisibility(VISIBLE);
        }else {
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx,heightPx);
    }

    protected void setSize(int widthPx, int heightPx) {
        //这里主要是做视频宽大与高,或者高大于宽时  视频的等比缩放
        int maxWidth= PixUtils.getScreenWidth();
        int maxHeight=maxWidth;

        int layoutWidth=maxWidth;
        int layoutHeight=0;

        int coverWidth;
        int coverHeight;

        if (widthPx>=heightPx){
            coverWidth=maxWidth;
            layoutHeight=coverHeight= (int) (heightPx/(widthPx*1.0f/maxWidth));
        }else {
            layoutHeight=coverHeight=maxHeight;
            coverWidth= (int) (widthPx/(heightPx*1.0f/maxHeight));
        }

        //设置各个长宽
        ViewGroup.LayoutParams params=getLayoutParams();
        params.width=layoutWidth;
        params.height=layoutHeight;
        setLayoutParams(params);

        //高斯模糊
        ViewGroup.LayoutParams blurParams=blur.getLayoutParams();
        blurParams.width=layoutWidth;
        blurParams.height=layoutHeight;
        blur.setLayoutParams(blurParams);

        //覆盖用的封面
        FrameLayout.LayoutParams coverParams= (LayoutParams) cover.getLayoutParams();
        coverParams.width=coverWidth;
        coverParams.height=coverHeight;
        coverParams.gravity= Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        //播放按钮
        FrameLayout.LayoutParams playBtnParams= (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity=Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);
    }
}
