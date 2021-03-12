package com.mooc.ppjoke.utils;
//专门用来获取Application对象

import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppGlobals {
    private static Application sApplication;

    public static Application getApplication() {
        if (sApplication == null) {
            try {
                sApplication = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication")
                        .invoke(null, (Object[]) null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }
}

//public class AppGlobals {
//    private static Application sApplication;
//
//    public static Application getApplication(){
//
//        //通过反射获取Application对象
//        if (sApplication==null){
//            try {
//                Method method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
//                //method不需要参数，所以直接调用直接传入null就可以
//                //invoke返回的值就是Application
//                method.invoke(null,null);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//
//        }
//        return sApplication;
//
//    }
//}
