package com.mooc.ppjoke;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public abstract class AbsViewModel<T> extends ViewModel {
    protected PagedList.Config config;
    private DataSource dataSource;
    private LiveData<PagedList<T>> pageData;
    private MutableLiveData<Boolean> boundaryPageData=new MutableLiveData<>();
    public AbsViewModel(){
         config=new PagedList.Config.Builder()
                 .setPageSize(10)
                 .setInitialLoadSizeHint(12)
//                 .setMaxSize(100)
//        .setEnablePlaceholders(false)
//                 .setPrefetchDistance()
                 .build();


         //获取一个LiveData对象
         pageData=new LivePagedListBuilder(factory,config)
                 .setInitialLoadKey(0)
//                 .setFetchExecutor()异步执行任务，paging框架会内置
                 .setBoundaryCallback(callback)
                 .build();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public LiveData<PagedList<T>> getPageData() {
        return pageData;
    }

    public MutableLiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    //PagedList数据被加载 情况的边界回调callback
    //但 不是每一次分页 都会回调这里，具体请看 ContiguousPagedList#mReceiver#onPageResult
    //deferBoundaryCallbacks
    PagedList.BoundaryCallback<T> callback=new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() {
            //新提交的PagedList中没有数据
            boundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            //新提交的PagedList中第一条数据被加载到列表上
            boundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            //新提交的PagedList中最后一条数据被加载到列表上
        }
    };



    DataSource.Factory factory=new DataSource.Factory() {
        @NonNull
        @Override
        public DataSource create() {
            if (dataSource==null||dataSource.isInvalid()){
                dataSource=createDataSource();
            }
            return dataSource;
        }
    };

    public abstract DataSource createDataSource();
}
