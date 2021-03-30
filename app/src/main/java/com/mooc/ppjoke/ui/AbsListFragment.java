package com.mooc.ppjoke.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mooc.libcommon.view.EmptyView;
import com.mooc.ppjoke.AbsViewModel;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.LayoutRefreshViewBinding;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsListFragment<T,M extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {

    protected LayoutRefreshViewBinding binding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    //继承自RecyclerView.Adapter
    protected PagedListAdapter<T,RecyclerView.ViewHolder> adapter;

    protected M mViewModel;
    protected DividerItemDecoration decoration;

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

        //默认给列表中的Item 一个 10dp的ItemDecoration
        decoration=new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));

        afterCreateView();
        return binding.getRoot();

//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected abstract void afterCreateView();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParameterizedType type= (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments=type.getActualTypeArguments();
        if (arguments.length>1){
            Type argument=arguments[1];
            Class modelClaz=((Class)argument).asSubclass(AbsViewModel.class);

            //触发页面初始化数据加载的逻辑
            mViewModel.getPageData().observe(getViewLifecycleOwner(), new Observer<PagedList<T>>() {
                @Override
                public void onChanged(PagedList<T> pagedList) {
                    adapter.submitList(pagedList);
                }
            });
            //监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean hasData) {
                    finishRefresh(hasData);
                }
            });

        }
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
