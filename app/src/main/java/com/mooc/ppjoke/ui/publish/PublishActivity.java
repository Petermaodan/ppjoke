package com.mooc.ppjoke.ui.publish;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mooc.libcommon.dialog.LoadingDialog;
import com.mooc.libcommon.utils.FileUtils;
import com.mooc.libnetwork.ApiResponse;
import com.mooc.libnetwork.ApiService;
import com.mooc.libnetwork.JsonCallback;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.ActivityLayoutPublishBinding;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.model.TagList;
import com.mooc.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLayoutPublishBinding mBinding;
    private int width,height;
    private String filePath,coverFilePath;
    private boolean isVideo;
    private UUID coverUploadUUID,fileUploadUUID;
    private String coverUploadUrl,fileUploadUrl;
    private TagList mTagList;
    private LoadingDialog mLoadingDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= DataBindingUtil.setContentView(this, R.layout.activity_layout_publish);

        mBinding.actionClose.setOnClickListener(this);
        mBinding.actionPublish.setOnClickListener(this);
        mBinding.actionDeleteFile.setOnClickListener(this);
        mBinding.actionAddTag.setOnClickListener(this);
        mBinding.actionAddFile.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.action_publish:
                publish();
                break;
            case R.id.action_add_tag:
                TagBottomSheetDialogFragment fragment=new TagBottomSheetDialogFragment();
                fragment.setOnTagItemSelectedListener(tagList -> {
                    mBinding.actionAddTag.setText(tagList.title);
                });
                fragment.show(getSupportFragmentManager(),"tag_dialog");
                break;
            case R.id.action_add_file:
                //需要重写publishActivity的onActivityResult方法
                CaptureActivity.startActivityForResult(this);
        }
    }

    private void publish() {
        showLoading();
        List<OneTimeWorkRequest> workRequests=new ArrayList<>();
        if (!TextUtils.isEmpty(filePath)){
            if (isVideo){
                //生成视频封面文件
                FileUtils.generateVideoCover(filePath).observe(this,coverPath -> {
                    coverFilePath=coverPath;

                    OneTimeWorkRequest request=getOneTimeWorkRequest(coverPath);
                    coverUploadUUID=request.getId();
                    workRequests.add(request);

                    //加入队列
                    enqueue(workRequests);
                });
            }
        }
    }

    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation= WorkManager.getInstance(PublishActivity.this).beginWith(workRequests);
        workContinuation.enqueue();

        workContinuation.getWorkInfosLiveData().observe(PublishActivity.this,workInfos -> {
            //block runing enuqued failed susscess finish
            int completedCount=0;
            int failedCount=0;
            for (WorkInfo workInfo : workInfos) {
                WorkInfo.State state=workInfo.getState();
                Data outputData=workInfo.getOutputData();
                UUID uuid=workInfo.getId();
                if (state==WorkInfo.State.FAILED){
                    // if (uuid==coverUploadUUID)是错的
                    if (uuid.equals(coverUploadUUID)){
                        showToast(getString(R.string.file_upload_cover_message));
                    }else if (uuid.equals(fileUploadUUID)){
                        showToast(getString(R.string.file_upload_oriainal_message));
                    }
                    failedCount++;
                }else if (state==WorkInfo.State.SUCCEEDED){
                    String fileUrl=outputData.getString("fileUrl");
                    if (uuid.equals(coverUploadUUID)){
                        coverUploadUrl=fileUrl;
                    }else if (uuid.equals(fileUploadUUID)){
                        fileUploadUrl=fileUrl;
                    }
                    completedCount++;
                }
            }

            //文件上传完毕
            if (completedCount>=workInfos.size()){
                publishFeed();
            }else if (failedCount>0){
                dismissLoading();
            }

        });
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl",coverUploadUrl)
                .addParam("fileUrl",fileUploadUrl)
                .addParam("fileWidth",width)
                .addParam("fileHeight",height)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId",mTagList==null?"":mTagList.tagId)
                .addParam("tagTitle",mTagList==null?"":mTagList.title)
                .addParam("feedText",mBinding.inputView.getText().toString())
                .addParam("feedType",isVideo? Feed.TYPE_VIDEO:Feed.TYPE_IMAGE_TEXT)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        showToast(getString(R.string.feed_publisj_success));
                        PublishActivity.this.finish();
                        dismissLoading();
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                        dismissLoading();
                    }
                });

    }

    private void showLoading() {
        if (Looper.myLooper()==Looper.getMainLooper()){
            if (mLoadingDialog==null){
                mLoadingDialog=new LoadingDialog(this);
                mLoadingDialog.setLoadingText(getString(R.string.feed_publish_ing));
            }
            mLoadingDialog.show();
        }
    }

    private void dismissLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } else {
            runOnUiThread(() -> {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            });
        }
    }

    private void showToast(String message) {
        if (Looper.myLooper()==Looper.getMainLooper()){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else {
            runOnUiThread(() -> {
                Toast.makeText(PublishActivity.this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private OneTimeWorkRequest getOneTimeWorkRequest(String filePath) {
        Data inputData=new Data.Builder()
                .putString("file",filePath)
                .build();

        //        @SuppressLint("RestrictedApi") Constraints constraints = new Constraints();
//        //设备存储空间充足的时候 才能执行 ,>15%
//        constraints.setRequiresStorageNotLow(true);
//        //必须在执行的网络条件下才能好执行,不计流量 ,wifi
//        constraints.setRequiredNetworkType(NetworkType.UNMETERED);
//        //设备的充电量充足的才能执行 >15%
//        constraints.setRequiresBatteryNotLow(true);
//        //只有设备在充电的情况下 才能允许执行
//        constraints.setRequiresCharging(true);
//        //只有设备在空闲的情况下才能被执行 比如息屏，cpu利用率不高
//        constraints.setRequiresDeviceIdle(true);
//        //workmanager利用contentObserver监控传递进来的这个uri对应的内容是否发生变化,当且仅当它发生变化了
//        //我们的任务才会被触发执行，以下三个api是关联的
//        constraints.setContentUriTriggers(null);
//        //设置从content变化到被执行中间的延迟时间，如果在这期间。content发生了变化，延迟时间会被重新计算
        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerContentUpdateDelay(0);
//        //设置从content变化到被执行中间的最大延迟时间
        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerMaxContentDelay(0);
        OneTimeWorkRequest request=new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                .setInputData(inputData)
        //                .setConstraints(constraints)
//                //设置一个拦截器，在任务执行之前 可以做一次拦截，去修改入参的数据然后返回新的数据交由worker使用
//                .setInputMerger(null)
//                //当一个任务被调度失败后，所要采取的重试策略，可以通过BackoffPolicy来执行具体的策略
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
//                //任务被调度执行的延迟时间
//                .setInitialDelay(10, TimeUnit.SECONDS)
//                //设置该任务尝试执行的最大次数
//                .setInitialRunAttemptCount(2)
//                //设置这个任务开始执行的时间
//                //System.currentTimeMillis()
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
//                //指定该任务被调度的时间
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
//                //当一个任务执行状态编程finish时，又没有后续的观察者来消费这个结果，难么workamnager会在
//                //内存中保留一段时间的该任务的结果。超过这个时间，这个结果就会被存储到数据库中
//                //下次想要查询该任务的结果时，会触发workmanager的数据库查询操作，可以通过uuid来查询任务的状态
//                .keepResultsForAtLeast(10, TimeUnit.SECONDS)
        .build();
        return request;
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.publish_exit_message)
                .setNegativeButton(R.string.publish_exit_action_cancel,null)
                .setPositiveButton(R.string.publish_exit_action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        PublishActivity.this.finish();
                    }
                }).create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK&&requestCode==CaptureActivity.REQ_CAPTURE&&data!=null){
            width=data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH,0);
            height=data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT,0);
            filePath=data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            isVideo=data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE,false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)){
            return;
        }

        //接收完文件的信息之后，需要将文件的缩略图显示出来，同时将addFiles的按钮隐藏掉。
        // 如果是视频类型，还需要将icon显示出来。文件删除之后，需要将addFile的按钮显示出来，
        // 同时将接收的数据置为空。
        mBinding.actionAddFile.setVisibility(View.GONE);
        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.cover.setImageUrl(filePath);
        mBinding.videoIcon.setVisibility(isVideo?View.VISIBLE:View.GONE);
        mBinding.cover.setOnClickListener(view -> {
            //预览
            PreviewActivity.startActivityForResult(PublishActivity.this,filePath,isVideo,null);
        });
    }
}