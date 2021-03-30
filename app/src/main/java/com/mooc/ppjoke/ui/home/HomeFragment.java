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
import androidx.paging.PagedListAdapter;

import com.mooc.libnavannotation.FragmentDestination;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.ui.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {



    @Override
    protected void afterCreateView() {
        mViewModel.getCacheLiveData().obseve

    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType=getArguments()==null?"all":getArguments().getString("feedType");
        return new FeedAdapter(getContext(),feedType);
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
}