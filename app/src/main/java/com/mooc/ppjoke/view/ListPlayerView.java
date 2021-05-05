package com.mooc.ppjoke.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.mooc.libcommon.utils.PixUtils;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.exoplayer.IPlayTarget;
import com.mooc.ppjoke.exoplayer.PageListPlay;
import com.mooc.ppjoke.exoplayer.PageListPlayManager;

/**
 * 列表视频播放专用
 */
public class ListPlayerView extends FrameLayout implements IPlayTarget,PlayerControlView.VisibilityListener,Player.EventListener {

    public View bufferVeiw;
    public PPImageView cover,blur;
    protected ImageView playBtn;



    protected String mCategory;
    protected int mWidthPx;
    protected int mHeightPx;
    protected String mVideoUrl;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击该区域时 我们诸主动让视频控制器显示出来
        PageListPlay pageListPlay= PageListPlayManager.get(mCategory);
        pageListPlay.controlView.show();
        return true;
    }

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public void onActive(){
        //视频播放,或恢复播放

        //通过该View所在页面的mCategory(比如首页列表tab_all,沙发tab的tab_video,标签帖子聚合的tag_feed) 字段，
        //取出管理该页面的Exoplayer播放器，ExoplayerView播放View,控制器对象PageListPlay
        PageListPlay pageListPlay=PageListPlayManager.get(mCategory);
        PlayerView playerView=pageListPlay.playerView;
        PlayerControlView controlView=pageListPlay.controlView;
        SimpleExoPlayer exoPlayer=pageListPlay.exoPlayer;
        if (playerView==null){
            return;
        }

        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView,true);
        ViewParent parent=playerView.getParent();
        if (parent!=this){
            //把展示视频画面的View添加到ItemView的容器上
            if (parent!=null){
                ((ViewGroup)parent).removeView(playerView);
                //还应该暂停掉列表上正在播放的那个
                ((ListPlayerView)parent).inActive();
            }

            ViewGroup.LayoutParams coverParams=cover.getLayoutParams();
            this.addView(playerView,1,coverParams);
        }

        ViewParent ctrlParent=controlView.getParent();
        if (ctrlParent!=this){
            //把视频控制器 添加到ItemView的容器上
            if (ctrlParent!=null){
                ((ViewGroup)ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity=Gravity.BOTTOM;
            this.addView(controlView,params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl,mVideoUrl)){
            onPlayerStateChanged(true, Player.STATE_READY);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void inActive() {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    public View getPlayController() {
        PageListPlay listPlay=PageListPlayManager.get(mCategory);
        return listPlay.controlView;
    }
}
