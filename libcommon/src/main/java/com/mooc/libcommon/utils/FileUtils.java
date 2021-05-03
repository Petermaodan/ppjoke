package com.mooc.libcommon.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 创建读取视频封面的方法，FileUtils，判断视频是否长传成功。只需要获取一帧就可以获取封面，
 * MediaMetadataRetriever retriever = new MediaMetadataRetriever();
 * 通过创建retriever方法，调用setDataSource方法来获取视频第一帧，由于是耗时操作，所以要在子线程完成该逻辑。之后创建compressBitmap方法对封面进行压缩。将压缩之后的数组写进文件中。
 * 文件写入成功之后，通过LiveData的postValue方法将file的path发送出去。
 */
public class FileUtils {
    /**
     * 截取视频文件的封面图
     *
     * @param filePath
     * @return
     */
    @SuppressLint("RestrictedApi")
    public static LiveData<String> generateVideoCover(final String filePath){
        final MutableLiveData<String> liveData=new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            //bugfix:此处应该使用{getFrameAtTime} 获取默认的第一个关键帧
            Bitmap frame=retriever.getFrameAtTime();
            FileOutputStream fos=null;
            if (frame!=null){
                //压缩到200k以下，再存储到本地文件中
                byte[] bytes=compressBitmap(frame,200);
                File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),System.currentTimeMillis()+".jpeg");

                try {
                    file.createNewFile();
                    fos=new FileOutputStream(file);
                    fos.write(bytes);
                    liveData.postValue(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (fos!=null){
                        try {
                            fos.flush();
                            fos.close();
                            fos=null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else {
                liveData.postValue(null);
            }

        });
        return liveData;
    }
    //循环压缩
    private static byte[] compressBitmap(Bitmap frame, int limit) {
        if (frame!=null&&limit>0){
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            int options=100;
            frame.compress(Bitmap.CompressFormat.JPEG,options,baos);
            while (baos.toByteArray().length>limit*1024){
                baos.reset();
                options-=5;
                frame.compress(Bitmap.CompressFormat.JPEG,options,baos);
            }
            byte[] bytes=baos.toByteArray();
            if (baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos=null;
            }
            return bytes;
        }
        return null;
    }
}
