package com.mooc.libnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data=jsonObject.getJSONObject("data");
        if (data!=null){
            Object data1 = data.get("data");
            JSON.parseObject(data1.toString());
            return JSON.parseObject(data1.toString(),type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class claz) {
        return null;
    }
}
