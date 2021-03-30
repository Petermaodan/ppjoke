package com.mooc.ppjoke.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.mooc.libnetwork.ApiResponse;
import com.mooc.libnetwork.ApiService;
import com.mooc.libnetwork.JsonCallback;
import com.mooc.libnetwork.Request;
import com.mooc.ppjoke.AbsViewModel;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.ui.MutableDataSource;
import com.mooc.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean witchCache=true;
    private MutableLiveData<PagedList<Feed>> cacheLiveData=new MutableLiveData<>();
    private AtomicBoolean loadAfter=new AtomicBoolean(false);
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }

    ItemKeyedDataSource<Integer,Feed> mDataSource=new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(0,params.requestedLoadSize,callback);
            //加载初始化
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
//加载分页数据

        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
//能够向前加载数据
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return null;
        }
    };

    private void loadData(int key,int count, ItemKeyedDataSource.LoadInitialCallback<Feed> callback) {
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", key)
                .addParam("pageCount", 10)
                .responseType(new TypeReference<ArrayList<Feed>>() {

                }.getType());

        if (witchCache){
            //获取本地缓存
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse response) {
                    Log.e("onCacheSuccess","onCacheSuccess");
//                    List<Feed> body=response.body;
                    MutableDataSource dataSource=new MutablePageKey
                }
            });

        }

        //获取网络请求
        try {
            Request netRequest=witchCache?request.clone():request;
            netRequest.cacheStrategy(key==0?Request.CACHE_ONLY:Request.NET_ONLY);
            ApiResponse<List<Feed>> response=netRequest.execute();
            List<Feed> data=response.body==null? Collections.emptyList():response.body;

            callback.onResult(data);


            if (key > 0) {
                //key>0表示上拉加载
                //通过BoundaryPageData发送数据 告诉UI层 是否应该主动关闭上拉加载分页的动画
                getBoundaryPageData().postValue(data.size()>0);
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }



    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()){
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id,config.pageSize,callback);
            }
        });
    }
}