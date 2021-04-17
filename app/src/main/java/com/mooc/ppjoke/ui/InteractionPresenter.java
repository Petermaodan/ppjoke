package com.mooc.ppjoke.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.mooc.libcommon.AppGlobals;
import com.mooc.libnetwork.ApiResponse;
import com.mooc.libnetwork.ApiService;
import com.mooc.libnetwork.JsonCallback;
import com.mooc.ppjoke.model.Comment;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.model.User;
import com.mooc.ppjoke.ui.login.UserManager;
import com.mooc.ppjoke.ui.share.ShareDialog;

import org.jetbrains.annotations.NotNull;

public class InteractionPresenter {

    public static final String DATA_FROM_INTERACTION="data_from_interaction";

    private static final String URL_TOGGLE_FEED_LIK = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_SHARE = "/ugc/increaseShareCount";

    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    //给一个帖子点赞/取消点赞，它和给帖子点踩一踩是互斥的

    public static void toggleFeedLike(LifecycleOwner owner, Feed feed){
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedLikeInternal(feed);
            }
        })){
        }else {
            toggleFeedLikeInternal(feed);
        }
    }



    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIK)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId",feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasLiked(hasLiked);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //给一个帖子点踩一踩/取消踩一踩,它和给帖子点赞是互斥的
    public static void toggleFeedDiss(LifecycleOwner owner,Feed feed){
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedDissInternal(feed);
            }
        })){}else {
            toggleFeedDissInternal(feed);
        }
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("userId",UserManager.get().getUserId())
                .addParam("itemId",feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body!=null){
                            boolean hasLiked=response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasdiss(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }


    //打开分享面板
    //解析一个format，进行网络请求，调用ApiService对Url进行解析，观察分享接口，只需要传入一个itemId即可。
    public static void openShare(Context context,Feed feed){
        String url="http://h5.aliyun.ppjoke.com/item/%s?timestamp=%s&user_id=%s";


        String shareContent=feed.feeds_text;
        if (!TextUtils.isEmpty(feed.url)){
            shareContent=feed.url;
        }else if (!TextUtils.isEmpty(feed.cover)){
            shareContent=feed.cover;
        }
        ShareDialog shareDialog=new ShareDialog(context);
        shareDialog.setShareContent(shareContent);
        shareDialog.setShareItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApiService.get(URL_SHARE)
                        .addParam("itemId",feed.itemId)
                        .execute(new JsonCallback<JSONObject>() {
                            @Override
                            public void onSuccess(ApiResponse<JSONObject> response) {
                                if (response.body!=null){
                                    int count=response.body.getIntValue("count");
                                    feed.getUgc().setShareCount(count);
                                }
                            }

                            @Override
                            public void onError(ApiResponse response) {
                                showToast(response.message);
                            }
                        });
            }
        });
        shareDialog.show();
    }

    //给一个帖子的评论点赞/取消点赞
    //观察toggleCommentLike接口，需要哪线参数，需要commentId，在toggleCommentLike()方法中也要传入LifecycleOwner用来判断是否已经登录，
    // 如果没有登录，还要唤醒登录页面（通过LiveData来观察是否已经登录），第二个参数是Comment,指这一条评论，创建toggleCommentLikeInternal()方法，
    // 同样在里面调用ApiService对URL进行解析，传入commentId和userId,同样，当评论成功之后，也要进行刷新，在Comment中创建一个getUgc()
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment){
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleCommentLikeInternal(comment);
            }
        }))
    }

    private static void toggleCommentLikeInternal(Comment comment) {
        //ApiService对url进行解析
        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId",comment.commentId)
                .addParam("userId",UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body!=null){
                            boolean hasLiked=response.body.getBooleanValue("hasLiked");
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()){
            return true;
        }else {
            LiveData<User> liveData=UserManager.get().login(AppGlobals.getApplication());
            if (owner==null){
                liveData.observeForever(loginObserver(observer,liveData));
            }else {
                liveData.observe(owner,loginObserver(observer,liveData));
            }
            return false;
        }
    }

    @NotNull
    private static Observer<User> loginObserver(Observer<User> observer, LiveData<User> liveData) {
        return new Observer<User>() {
            @Override
            public void onChanged(User user) {
                liveData.removeObserver(this);
                if (user!=null&&observer!=null){
                    observer.onChanged(user);
                }
            }
        };
    }

    //收藏/取消收藏一个帖子
    public static void toggleFeedFavorite(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedFavorite(feed);
            }
        })) {
        } else {
            toggleFeedFavorite(feed);
        }
    }

    private static void toggleFeedFavorite(Feed feed) {
        ApiService.get("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //关注/取消关注用户
    public static void toggleFollowUser(LifecycleOwner owner,Feed feed){
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFollowUser(feed);
            }
        })){

        }else {
            toggleFollowUser(feed);
        }
    }

    private static void toggleFollowUser(Feed feed) {
        ApiService.get("/ugc/toggleUserFollow")
                .addParam("followUserId",UserManager.get().getUserId())
                .addParam("userId",feed.author.userId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body!=null){
                            boolean hasFollow=response.body.getBooleanValue("hasLiked");
                            feed.getAuthor().setHasFollow(hasFollow);
                            LiveDataBus
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }
}
