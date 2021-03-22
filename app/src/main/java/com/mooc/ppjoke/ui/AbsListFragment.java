package com.mooc.ppjoke.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mooc.libcommon.view.EmptyView;
import com.mooc.ppjoke.databinding.LayoutRefreshViewBinding;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;

public abstract class AbsListFragment<T> extends Fragment {

    protected LayoutRefreshViewBinding binding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    //继承自RecyclerView.Adapter
    protected PagedListAdapter<T,RecyclerView.ViewHolder> adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=LayoutRefreshViewBinding.inflate(inflater,container,false);
        binding.getRoot().setFitsSystemWindows(true);
        mRecyclerView=binding.recyclerView;
        mRefreshLayout=binding.refreshLayout;
        mEmptyView=binding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        adapter=getAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setItemAnimator(null);

        return binding.getRoot();




        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public void submitList(PagedList<T> result){
        //只有当新数据集合大于0 的时候，才调用adapter.submitList
        //否则可能会出现 页面----有数据----->被清空-----空布局
        if (result.size()>0){
            adapter.submitList(result);
        }
        finishRefresh(result.size()>0);
    }

    public void finishRefresh(boolean hasData) {
        PagedList<T> currentList=adapter.getCurrentList();
        hasData=hasData||currentList!=null&&currentList.size()>0;
        RefreshState state=mRefreshLayout.getState();
        if (state.isFooter&&state.isOpening){
            mRefreshLayout.finishLoadMore();
        }else if (state.isHeader&&state.isOpening){
            mRefreshLayout.finishRefresh();
        }

        if (hasData){
            mEmptyView.setVisibility(View.GONE);
        }else {
            mEmptyView.setVisibility(View.VISIBLE);
        }

    }


    public abstract PagedListAdapter getAdapter();
}
