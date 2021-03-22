package com.mooc.ppjoke.model;

public class Feed {

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

}
