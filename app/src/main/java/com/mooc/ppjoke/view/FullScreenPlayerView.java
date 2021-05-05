package com.mooc.ppjoke.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.mooc.libcommon.utils.PixUtils;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.exoplayer.PageListPlay;
import com.mooc.ppjoke.exoplayer.PageListPlayManager;

/**
 * 视频详情页全屏播放专用
 */
public class FullScreenPlayerView extends ListPlayerView {
    private PlayerView exoPlayerView;

    public FullScreenPlayerView(@NonNull Context context) {
        super(context);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        exoPlayerView= (PlayerView) LayoutInflater.from(context).inflate(R.layout.layout_exo_player_view,null,false);
    }


    /**
     * 断视频的高度和宽度，如果宽度大于高度，那就不变，反之就需要屏幕的宽和高和视频的宽和高相同，等比缩放
     * @param widthPx
     * @param heightPx
     */
    @Override
    protected void setSize(int widthPx, int heightPx) {
        if (widthPx>=heightPx){
            super.setSize(widthPx,heightPx);
            return;
        }
        int maxWidth= PixUtils.getScreenWidth();
        int maxHeight=PixUtils.getScreenHeight();

        ViewGroup.LayoutParams params=getLayoutParams();
        params.width=maxWidth;
        params.height=maxHeight;
        setLayoutParams(params);

        FrameLayout.LayoutParams coverLayoutParams= (LayoutParams) cover.getLayoutParams();
        coverLayoutParams.width= (int) (widthPx/(heightPx*1.0f/maxHeight));
        coverLayoutParams.height=maxHeight;
        coverLayoutParams.gravity= Gravity.CENTER;
        cover.setLayoutParams(coverLayoutParams);
    }

    //保证手指滑动时可以等比缩放
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (mHeightPx>mWidthPx){
            int layoutWidth=params.width;
            int layoutHeight=params.height;
            ViewGroup.LayoutParams coverLayoutParams=cover.getLayoutParams();
            coverLayoutParams.width= (int) (mWidthPx/(mHeightPx*1.0f/layoutHeight));
            coverLayoutParams.height=layoutHeight;

            cover.setLayoutParams(coverLayoutParams);
            //给exoPlayerView设置等比缩放效果。
            if (exoPlayerView!=null){
                ViewGroup.LayoutParams layoutParams=exoPlayerView.getLayoutParams();
                if (layoutParams!=null&&layoutParams.width>0&&layoutParams.height>0){
                    float scalex=coverLayoutParams.width*1.0f/layoutParams.width;
                    float scaley=coverLayoutParams.height*1.0f/layoutParams.height;

                    //之后在exoPlayerView中设置这些值。
                    exoPlayerView.setScaleX(scalex);
                    exoPlayerView.setScaleY(scaley);
                }
            }
        }
        super.setLayoutParams(params);
    }

    @Override
    public void onActive() {
        //视频播放,或恢复播放

        //通过该View所在页面的mCategory(比如首页列表tab_all,沙发tab的tab_video,标签帖子聚合的tag_feed) 字段，
        //取出管理该页面的Exoplayer播放器，ExoplayerView播放View,控制器对象PageListPlay
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = pageListPlay.playerView;
        PlayerControlView controlView = pageListPlay.controlView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }

        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView, true);
        ViewParent parent = playerView.getParent();
        if (parent != this) {

            //把展示视频画面的View添加到ItemView的容器上
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                //还应该暂停掉列表上正在播放的那个
                ((ListPlayerView) parent).inActive();
            }

            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            //把视频控制器 添加到ItemView的容器上
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlay.playUrl = mVideoUrl;
        }
        controlView.show();
        controlView.setVisibilityListener(this);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void inActive() {
        super.inActive();
        PageListPlay pageListPlay=PageListPlayManager.get(mCategory);
        //主动切断exoplayer与视频播放器的联系
        pageListPlay.switchPlayerView(exoPlayerView,false);
    }
}
