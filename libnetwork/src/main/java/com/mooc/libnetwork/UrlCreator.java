package com.mooc.libnetwork;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlCreator {

    //将所有的params拼接到url后面
    public static String createUrlFromParams(String url, Map<String,Object> params){
        StringBuilder builder=new StringBuilder();
        builder.append(url);
        if(url.indexOf("?")>0||url.indexOf("&")>0){
            builder.append("&");
        }else {
            builder.append("?");
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String value= null;
            try {
                value = URLEncoder.encode(String.valueOf(entry.getValue()),"UTF-8");
                builder.append(entry.getKey()).append("=").append(value).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //需要把第二个多出来的&删除
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();


    }
}
