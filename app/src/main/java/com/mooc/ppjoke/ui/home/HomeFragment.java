package com.mooc.ppjoke.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.mooc.libnavannotation.FragmentDestination;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.exoplayer.PageListPlayDetector;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.ui.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {


    private PageListPlayDetector playDetector;
    private String feedType;
    private boolean shouldPause;

    public static HomeFragment newInstance(String feedType) {
        Bundle args=new Bundle();
        args.putString("feedType",feedType);
        HomeFragment fragment=new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        mViewModel.getCacheLiveData().obseve

    }

    //将检测列表自动播放逻辑进行更新，复写getAdapter方法。
    @Override
    public PagedListAdapter getAdapter() {
        feedType=getArguments()==null?"all":getArguments().getString("feedType");
        return new FeedAdapter(getContext(),feedType){
            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo=feed.itemType==Feed.TYPE_VIDEO;
                shouldPause=!isVideo;
            }

            @Override
            protected void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()){
                    playDetector.addTarget(holder.getListplayerView());
                }
            }

            @Override
            protected void onViewDetachedFromWindow2(ViewHolder holder) {
                playDetector.removeTarget(holder.getListplayerView());
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                //这个方法是在我们每提交一次 pagelist对象到adapter 就会触发一次
                //每调用一次 adpater.submitlist
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        //上拉分页
        Feed feed = adapter.getCurrentList().get(adapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id,new ItemKeyedDataSource)
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            playDetector.onPause();
        }else {
            playDetector.onResume();
        }
    }
}