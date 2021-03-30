package com.mooc.libnetwork;
//返回值的类型-对返回体结果的一个包装
public class ApiResponse<T> {
    public boolean success;
    public int status;
     public String message;
     public T body;
}
