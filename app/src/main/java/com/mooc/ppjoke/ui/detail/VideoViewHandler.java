package com.mooc.ppjoke.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;

import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.LayoutFeedDetailTypeVideoBinding;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.view.FullScreenPlayerView;

public class VideoViewHandler extends ViewHandler {
    private final LayoutFeedDetailTypeVideoBinding mVideoBinding;
    private final FullScreenPlayerView playerView;
    private final CoordinatorLayout coordinator;
    private String category;
    private boolean backPressed;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mVideoBinding= DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_video);

        mInateractionBinding=mVideoBinding.bottomInteraction;
        mRecyclerView=mVideoBinding.recyclerView;
        playerView=mVideoBinding.playerView;
        coordinator=mVideoBinding.coordinator;

        View authorInfoView=mVideoBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        params.setBehavior(new ViewAnchorBehavior(R.id.player_view));


        mVideoBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        });

        CoordinatorLayout.LayoutParams layoutParams= (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior= (ViewZoomBehavior) layoutParams.getBehavior();
        behavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom=playerView.getBottom();
                boolean moveUp=height<bottom;
                boolean fullscreen=moveUp?height>=coordinator.getBottom()-mInateractionBinding.getRoot().getHeight()
                        :height>=coordinator.getBottom();
                setViewAppearance(fullscreen);
            }
        });


    }


    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);

        category=mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        playerView.bindData(category,mFeed.width,mFeed.height,mFeed.cover,mFeed.url);

        //这里需要延迟一帧 等待布局完成，再来拿playerView的bottom值 和 coordinator的bottom值
        //做个比较。来校验是否进入详情页时时视频在全屏播放

        playerView.post(() ->{
            boolean fullscreen=playerView.getBottom()>=coordinator.getBottom();
            setViewAppearance(fullscreen);
        });

        //给headerView、 绑定数据并添加到列表之上
        LayoutFeedDetailTypeVideoBinding headerBinding=LayoutFeedDetailTypeVideoBinding.inflate(
                LayoutInflater.from(mActivity),
                mRecyclerView,
                false
        );
        headerBinding.setFeed(mFeed);
        listAdapter.addHeaderView(headerBinding.getRoot());
    }

    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mInateractionBinding.setFullscreen(fullscreen);
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen?View.VISIBLE:View.GONE);

        //底部互动区的高度
        int inputHeight=mInateractionBinding.getRoot().getMeasuredHeight();
        //播放控制器的高度
        int ctrlViewHeight=playerView.getPlayController().getMeasuredHeight();
        //播放控制器bottom值
        int bottom=playerView.getPlayController().getBottom();
        //全屏播放，播放控制器需要处在底部互动区域的上面
        playerView.getPlayController().setY(fullscreen?bottom-inputHeight-ctrlViewHeight:bottom-ctrlViewHeight);

        mInateractionBinding.inputView.setBackgroundResource(fullscreen?R.drawable.bg_edit_view2:R.drawable.bg_edit_view);
    }

    @Override
    public void onPause() {
        super.onPause();
        //除了普通暂停外，主动切断exoplayer与视频播放器的联系
        if (!backPressed){
            playerView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed=false;
        playerView.onActive();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed=true;
        //按了返回键后需要 恢复 播放控制器的位置。否则回到列表页时 可能会不正确的显示
        playerView.getPlayController().setTranslationX(0);
    }
}
