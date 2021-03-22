package com.mooc.ppjoke.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mooc.libcommon.AppGlobals;
import com.mooc.ppjoke.model.BottomBar;
import com.mooc.ppjoke.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class AppConfig {
    //sDestConfig和sBottomBar都是应用在首页，所以定义为static,这样不会被销毁
    private static HashMap<String, Destination> sDestConfig;
    private static BottomBar sBottomBar;
    public static HashMap<String, Destination> getDestConfig(){
        //当sDestConfig为空的时候，我们就来实现他
        if (sDestConfig==null){
            String content = parseFile("destination.json");
            //TypeReference是什么？
            sDestConfig=JSON.parseObject(content,new TypeReference<HashMap<String,Destination>>(){}.getType());

        }
        return sDestConfig;
    }

    public static BottomBar getsBottomBar(){
        //若sBottomBar对象为空，我们才解析他
        if (sBottomBar==null){
            String content=parseFile("main_tabs_config.json");
            sBottomBar=JSON.parseObject(content,BottomBar.class);
        }
        return sBottomBar;
    }


    private static String parseFile(String fileName){

        AssetManager assets =
                AppGlobals.getApplication().getResources().getAssets();

        //对得到的文件输入流进行解析
        //将stream和reader放在外面，是为了最后将其close()
        //IO流逐行写出来
        InputStream stream = null;
        BufferedReader reader = null;
        StringBuilder builder=new StringBuilder();
        try {
            stream=assets.open(fileName);
            reader=new BufferedReader(new InputStreamReader(stream));
            String line=null;
            while ((line=reader.readLine())!=null){
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream!=null){
                    stream.close();
                }
                if (reader!=null){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();

    }

}
