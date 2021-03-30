package com.mooc.ppjoke.model;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.io.Serializable;

public class Ugc extends BaseObservable implements  Serializable {

    /**
     * likeCount : 111098
     * shareCount : 1104
     * commentCount : 10003
     * hasFavorite : false
     * hasLiked : false
     * hasdiss : false
     * hasDissed : false
     */

    public int likeCount;
    public int shareCount;
    public int commentCount;
    public boolean hasFavorite;
    public boolean hasLiked;
    public boolean hasdiss;
    public boolean hasDissed;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof Ugc))
            return false;
        Ugc newUgc = (Ugc) obj;
        return likeCount == newUgc.likeCount
                && shareCount == newUgc.shareCount
                && commentCount == newUgc.commentCount
                && hasFavorite == newUgc.hasFavorite
                && hasLiked == newUgc.hasLiked
                && hasdiss == newUgc.hasdiss;
    }

    //点赞
    public boolean isHasLiked(){
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked){
        if (this.hasLiked==hasLiked)return;
        if (hasLiked){
            likeCount=likeCount+1;
            //赞和踩是互斥的
            setHasdiss(false);
        }else {
            likeCount=likeCount-1;
        }
        this.hasLiked=hasLiked;
        notifyPropertyChanged(BR._all);
    }

    //踩
    public boolean isHasdiss(){return hasdiss;}

    public void setHasdiss(boolean hasdiss) {
        if (this.hasdiss=hasdiss){
            return;
        }
        if (hasdiss){
            //赞和踩是互斥的
            setHasdiss(false);
        }
        this.hasdiss=hasdiss;

        //重新执行数据的绑定
        notifyPropertyChanged(BR._all);
    }

    //收藏
    public boolean isHasFavorite(){
        return hasFavorite;
    }
    public void setHasFavorite(boolean hasFavorite){
        this.hasFavorite=hasFavorite;
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount=shareCount;
        //刷新
        notifyPropertyChanged(BR._all);
    }
}
