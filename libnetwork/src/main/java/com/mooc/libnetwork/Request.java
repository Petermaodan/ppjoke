package com.mooc.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.mooc.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class Request<T,R extends Request> {
    protected   String mUrl;
    protected HashMap<String,String> headers=new HashMap<>();
    protected HashMap<String,Object> params=new HashMap<>();

    //仅仅访问本地缓存，即使本地缓存不存在，也不会发起网络请求
    public static final int CACHE_ONLY=1;
    //先访问缓存，同时发起网络请求，成功后缓存到本地
    public static final int CACHE_FIRST=2;
    //仅仅访问服务器，不存任何储存
    public static final int NET_ONLY=3;
    //访问网络，成功后缓存到本地
    public static final int NET_CACHE=4;
    private String cacheKey;
    private Type mType;
    private Class mClaz;
    private int mCacheStrategy=NET_CACHE;


    public Request(String url){
        mUrl=url;
    }

    //使用注解对类型进行归纳
    @IntDef({CACHE_ONLY,CACHE_FIRST,NET_ONLY,NET_CACHE})
    public @interface CacheStrategy{

    }

    //请求头
    public R addHeader(String key,String value){
        headers.put(key,value);
        return (R) this;
    }

    public R addParam(String key,Object value){
//        if (value instanceof Integer)
        //通过Type字段来判断是否为八种基本类型
        try {
            Field field = value.getClass().getField("TYPE");
            Class claz= (Class) field.get(null);
            if (claz.isPrimitive()){
                params.put(key, value);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    //传入一个cache的类型
    public R cacheStrategy(@CacheStrategy int cacheStrategy){
        mCacheStrategy=cacheStrategy;
        return (R) this;
    }

    //用来缓存和用来读取
    public R cacheKey(String key){
        this.cacheKey=key;
        return (R) this;
    }


    //callback入参，表示是异步的，没有参数的是同步请求
    public ApiResponse<T> execute(){
        //同步的方法里面要不只能读取缓存，要不只能读取网络数据
        if (mCacheStrategy==CACHE_ONLY){
            return readCache();
        }
        ApiResponse<T> result=null;
        try {
            Response response = getCall().execute();
            result=parseResponse(response,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    //网络请求真正的方法
    //异步请求
    //@SuppressLint("RestrictedApi")注解是什么意思？
    @SuppressLint("RestrictedApi")
    public void execute(JsonCallback<T> callback){
        if (mCacheStrategy!=NET_ONLY){
            //子线程中读取
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response=readCache();
                    if (callback!=null){
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy!=CACHE_ONLY){
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> response=new ApiResponse<>();
                    response.message=e.getMessage();
                    callback.onError(response);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    //解析
                    ApiResponse<T> apiResponse=parseResponse(response,callback);
                    if (apiResponse.success){
                        callback.onError(apiResponse);
                    }else {
                        //两层成功才算成功
                        callback.onSuccess(apiResponse);
                    }
                }
            });
        }

    }

    private   ApiResponse<T> readCache() {
        //是否自动生成generateCacheKey()
        String key=TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result=new ApiResponse<>();
        result.status=304;
        result.message="缓存获取成功";
        result.body= (T) cache;
        result.success=true;
        return result;

    }

    private ApiResponse<T> parseResponse(Response response,JsonCallback<T> callback) {
        String message=null;
        int status=response.code();
        boolean success=response.isSuccessful();
        ApiResponse<T> result=new ApiResponse<>();
        Convert convert=ApiService.sConvert;
        try {
            String content=response.body().string();
            if (success){
//                String content = response.body().string();
                if(callback!=null){
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument=type.getActualTypeArguments()[0];
                    //解析通过Convert接口来转换
                    result.body= (T) convert.convert(content,mType);
                }else if (mType!=null){
                    result.body= (T) convert.convert(content,mType);

                }else if (mClaz!=null){
                    result.body= (T) convert.convert(content,mClaz);
                }else {
                    Log.e("request","parseResponse:无法解析");
                }
            }else {
                message=content;
            }
        } catch (IOException e) {
            message=e.getMessage();
            success=false;
        }
        result.success=success;
        result.status=status;
        result.message=message;
        if (mCacheStrategy!=NET_ONLY&&result.success&&result.body!=null&&result.body instanceof Serializable){
            saveCache(result.body);
        }
        return result;


    }

    private void saveCache(T body) {
        String key= TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        CacheManager.save(key,body);
    }

    private String generateCacheKey() {
        cacheKey=UrlCreator.createUrlFromParams(mUrl,params);
        return cacheKey;

    }

    //传入泛型参数
    public R responseType(Type type){
        mType=type;
        return (R)this;
    }

    public R responseType(Class claz){
        mType=claz;
        return (R)this;
    }


    private Call getCall() {
        okhttp3.Request.Builder builder=new okhttp3.Request.Builder();
        addHeaders(builder);
        //构造一个request的body
        okhttp3.Request request =generateRequest(builder);
        //将request传入ApiService的okHttpClient对象中得到Call对象
        Call call=ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(),entry.getValue());
        }

    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T,R>)super.clone();
    }
}
