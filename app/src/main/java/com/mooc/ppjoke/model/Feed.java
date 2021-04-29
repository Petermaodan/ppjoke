package com.mooc.ppjoke.model;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;

public class Feed extends BaseObservable implements Serializable {
    public static final int TYPE_IMAGE_TEXT=1;
    public static final int TYPE_VIDEO=2;

    /**
     * id : 1578922432
     * itemId : 1615197740459
     * itemType : 2
     * createTime : 1615197740459
     * duration : 0
     * feeds_text : 皮皮虾那个省的皮友最多?
     * authorId : 1578919786
     * activityIcon : null
     * activityText : 放松时刻
     * width : 720
     * height : 1280
     * url : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/maohelaoshu.mp4
     * cover : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/maohelaoshu.jpeg
     */

    public int id;
    public long itemId;
    public int itemType;
    public long createTime;
    public int duration;
    public String feeds_text;
    public int authorId;
    public String activityIcon;
    public String activityText;
    public int width;
    public int height;
    public String url;
    public String cover;
    public Ugc ugc;
    public User author;
    public Comment topComment;


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj==null||!(obj instanceof Feed))
            return false;

        Feed newFeed= (Feed) obj;
        return id==newFeed.id
                && itemId == newFeed.itemId
                && itemType == newFeed.itemType
                && createTime == newFeed.createTime
                && duration == newFeed.duration
                && TextUtils.equals(feeds_text, newFeed.feeds_text)
                && authorId == newFeed.authorId
                && TextUtils.equals(activityIcon, newFeed.activityIcon)
                && TextUtils.equals(activityText, newFeed.activityText)
                && width == newFeed.width
                && height == newFeed.height
                && TextUtils.equals(url, newFeed.url)
                && TextUtils.equals(cover, newFeed.cover)
                && (author != null && author.equals(newFeed.author))
                && (topComment != null && topComment.equals(newFeed.topComment))
                && (ugc != null && ugc.equals(newFeed.ugc));
    }

    @Bindable
    public Ugc getUgc() {
        if(ugc==null){
            ugc=new Ugc();
        }
        return ugc;
    }

    @Bindable
    public User getAuthor() {
        return author;
    }
}
