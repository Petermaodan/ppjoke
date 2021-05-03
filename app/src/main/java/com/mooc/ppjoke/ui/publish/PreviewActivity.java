package com.mooc.ppjoke.ui.publish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.ActivityLayoutPreviewBinding;

import java.io.File;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLayoutPreviewBinding mPreviewBinding;
    public static final String KEY_PREVIEW_URL = "preview_url";
    public static final String KEY_PREVIEW_VIDEO = "preview_video";
    public static final String KEY_PREVIEW_BTNTEXT = "preview_btntext";
    public static final int REQ_PREVIEW = 1000;
    private SimpleExoPlayer player;

    //将传递进去的参数传递给Intent对象即可，需要REQ_PREVIEW,
    public static void startActivityForResult(Activity activity,String previewUrl,boolean isVideo,String btnText){
        Intent intent=new Intent(activity,PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL,previewUrl);
        intent.putExtra(KEY_PREVIEW_VIDEO,isVideo);
        intent.putExtra(KEY_PREVIEW_BTNTEXT,btnText);
        activity.startActivityForResult(intent,REQ_PREVIEW);
        activity.overridePendingTransition(0,0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreviewBinding= DataBindingUtil.setContentView(this,R.layout.activity_layout_preview);
        String previewUrl=getIntent().getStringExtra(KEY_PREVIEW_URL);
        boolean isVideo=getIntent().getBooleanExtra(KEY_PREVIEW_VIDEO,false);
        String btnText=getIntent().getStringExtra(KEY_PREVIEW_BTNTEXT);
        if (TextUtils.isEmpty(btnText)){
            mPreviewBinding.actionOk.setVisibility(View.GONE);
        }else {
            mPreviewBinding.actionOk.setVisibility(View.VISIBLE);
            mPreviewBinding.actionOk.setText(btnText);
            mPreviewBinding.actionOk.setOnClickListener(this);

        }

        if (isVideo){
            previewVideo(previewUrl);
        }else {
            previewImage(previewUrl);
        }
    }

    private void previewVideo(String previewUrl) {
        mPreviewBinding.playerView.setVisibility(View.VISIBLE);
        player= ExoPlayerFactory.newSimpleInstance(this,new DefaultRenderersFactory(this),new DefaultTrackSelector(),new DefaultLoadControl());

        Uri uri=null;
        File file=new File(previewUrl);
        if (file.exists()){
            DataSpec dataSpec=new DataSpec(Uri.fromFile(file));
            FileDataSource fileDataSource=new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri=fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        }else {
            uri=uri.parse(previewUrl);
        }
        //对于网络视频的播放，需要创建一个ProgressiveMediaSource对象，最后将播放器和数组的生命周期相关联。暂停只需要调用onResume方法。

        ProgressiveMediaSource.Factory factory=new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, Util.getUserAgent(this,getPackageName())));
        ProgressiveMediaSource mediaSource=factory.createMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        mPreviewBinding.playerView.setPlayer(player);

    }

    private void previewImage(String previewUrl) {
        mPreviewBinding.photoView.setVisibility(View.VISIBLE);
        //直接将资源通过Glide载入布局文件
        Glide.with(this).load(previewUrl).into(mPreviewBinding.photoView);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.action_close){
            finish();
        }else if (v.getId()==R.id.action_ok){
            setResult(RESULT_OK,new Intent());
            finish();
        }
    }


}