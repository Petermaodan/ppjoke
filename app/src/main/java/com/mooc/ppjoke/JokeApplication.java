package com.mooc.ppjoke;
/**
 * 咱们的服务器已经部署到公网了.
 * <p>
 * 项目在线Api文档地址：http://123.56.232.18:8080/serverdemo/swagger-ui.html#/
 * <p>
 * 同学们有需要的话,可以按照:https://git.imooc.com/Chubby/jetpack_ppjoke/src/master/%e6%9c%8d%e5%8a%a1%e5%99%a8%e7%8e%af%e5%a2%83%e6%90%ad%e5%bb%ba.md
 * 来搭建本地服务器
 */
import android.app.Application;

import com.mooc.libnetwork.ApiService;

public class JokeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://192.168.1.121:8080/serverdemo", null);
    }
}
