package com.mooc.ppjoke.utils;

public class StringConvert {
    public static String converFeedUgc(int count){
        if (count<10000){
            return String.valueOf(count);
        }
        return count/10000+"字";
    }
}
