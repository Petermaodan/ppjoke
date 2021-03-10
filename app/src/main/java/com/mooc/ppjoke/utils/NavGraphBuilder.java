package com.mooc.ppjoke.utils;

import android.content.ComponentName;

import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.mooc.ppjoke.model.Destination;

import java.util.HashMap;

public class NavGraphBuilder {
    //需要和NavController相关联，所以该方法的参数就为此
    public static void build(NavController controller){
        //通过响应的Navigate创建相应的页面节点
        NavigatorProvider provider = controller.getNavigatorProvider();

        //分别获取FragmentNavigator和ActivityNavigator对象
        FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        ActivityNavigator activityNavigator=provider.getNavigator(ActivityNavigator.class);

        //后面的数据要添加到改NavGraph中
        NavGraph navGraph=new NavGraph(new NavGraphNavigator(provider));

        HashMap<String, Destination> destConfig=AppConfig.getDestConfig();

        for (Destination value : destConfig.values()) {
            if (value.isFragment){
                FragmentNavigator.Destination destination=fragmentNavigator.createDestination();

                //向destination中填相应的字段
                destination.setClassName(value.className);
                destination.setId(value.id);
                destination.addDeepLink(value.pageUrl);

                navGraph.addDestination(destination);
            }else {
                ActivityNavigator.Destination destination=activityNavigator.createDestination();
                destination.setId(value.id);
                destination.addDeepLink(value.pageUrl);
                destination.setComponentName(new ComponentName(AppGlobals.getApplication().getPackageName(),value.className));


                navGraph.addDestination(destination);
            }

            //如果value是默认启动页
            if (value.asStarter){
                navGraph.setStartDestination(value.id);
            }
        }
        //将navGraph赋值给controller
        controller.setGraph(navGraph);
    }
}
