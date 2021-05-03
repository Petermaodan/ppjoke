package com.mooc.ppjoke.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.mooc.libcommon.dialog.LoadingDialog;
import com.mooc.libcommon.global.AppGlobals;
import com.mooc.libcommon.utils.FileUploadManager;
import com.mooc.libcommon.utils.PixUtils;
import com.mooc.libcommon.view.ViewHelper;
import com.mooc.libnetwork.ApiResponse;
import com.mooc.libnetwork.ApiService;
import com.mooc.libnetwork.JsonCallback;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.LayoutCommentDialogBinding;
import com.mooc.ppjoke.model.Comment;
import com.mooc.ppjoke.ui.login.UserManager;
import com.mooc.ppjoke.ui.publish.CaptureActivity;

import org.w3c.dom.Text;

import java.util.concurrent.atomic.AtomicInteger;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {


    private static final String KEY_ITEM_ID = "key_item_id";
    //评论binding文件
    private LayoutCommentDialogBinding mBinding;
    private long itemId;
    private String filePath;
    private boolean isVideo;
    private int width,height;
    private commentAddListener mListener;
    private LoadingDialog loadingDialog;
    private String coverUrl;
    private String fileUrl;

    public static CommentDialog newInstance(long itemId) {
        Bundle args=new Bundle();
        args.putLong(KEY_ITEM_ID,itemId);
        CommentDialog fragment=new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window =getDialog().getWindow();
        window.setWindowAnimations(0);

        mBinding=LayoutCommentDialogBinding.inflate(inflater,((ViewGroup)window.findViewById(android.R.id.content)),false);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);

        //将结果回调出去，同样需要回到主线程，通过args模板，快速将itemid传入进去
        this.itemId=getArguments().getLong(KEY_ITEM_ID);

        ViewHelper.setViewOutline(mBinding.getRoot(), PixUtils.dp2px(10),ViewHelper.RADIUS_TOP);

        mBinding.getRoot().post(()->showSoftInputMethod());
        dismissWhenPressBack();

    }

    @Override
    public void onClick(View v) {
        if (v.getId()== R.id.comment_send){
            publishComment();
        }else if (v.getId()==R.id.comment_video){
            CaptureActivity.startActivityForResult(getActivity());
        }else if (v.getId()==R.id.comment_delete){
            filePath=null;
            isVideo=false;
            width=0;
            height=0;
            mBinding.commentCover.setImageDrawable(null);
            mBinding.commentExtLayout.setVisibility(View.GONE);

            mBinding.commentVideo.setEnabled(true);
            mBinding.commentVideo.setAlpha(255);
        }
    }

    private void publishComment() {

    }

    @SuppressLint("RestrictedApi")
    private void uploadFile(String coverPath, String filePath){
        //AtomicInteger, CountDownLatch, CyclicBarrier
        showLoadingDialog();
        //filePath不可能为空，初始文件不为空，所以给AtomicInteger设置为1
        AtomicInteger count=new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)){
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
                int remain=count.decrementAndGet();
                coverUrl= FileUploadManager.upload(coverPath);
                if (remain<=0){
                    if (!TextUtils.isEmpty(fileUrl)&&!TextUtils.isEmpty(coverUrl)){
                        publish();
                    }else {
                        dismissLoadingDialog();
                        showToast(getString(R.string.file_upload_failed));
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            int remain=count.decrementAndGet();
            fileUrl=FileUploadManager.upload(filePath);
            if (remain<=0){
                if (!TextUtils.isEmpty(filePath)||!TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)){
                   publish();
                }else {
                    dismissLoadingDialog();
                    showToast(getString(R.string.file_upload_failed));
                }
            }
        });

    }

    private void publish() {
        String commentText=mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("image_url", isVideo ? coverUrl : fileUrl)
                .addParam("video_url", isVideo ? fileUrl : null)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败:" + response.message);
                        dismissLoadingDialog();
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void onCommentSuccess(Comment body) {
        showToast("评论发布成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            if (mListener!=null){
                mListener.onAddComment(body);
            }
            dismiss();
        });
    }

    @SuppressLint("RestrictedApi")
    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper()==Looper.getMainLooper()){
            Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show();
        }else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CaptureActivity.REQ_CAPTURE&&resultCode== Activity.RESULT_OK){
            filePath=data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            width=data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH,0);
            height=data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT,0);
            isVideo=data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE,false);

            //让对话框中文件的缩略图进行显示，如果拍摄的视频类型的，还需要将commentVideoIcon显示出来
            if (!TextUtils.isEmpty(filePath)){
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                if (isVideo){
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }

            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setAlpha(80);
        }
    }

    private void showLoadingDialog(){
        if (loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
            loadingDialog.setLoadingText(getString(R.string.upload_text));
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }
        if (!loadingDialog.isShowing()){
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog(){
        if (loadingDialog!=null){
            //dismissLoadingDialog  的调用可能会出现在异步线程调用
            if (Looper.myLooper()==Looper.getMainLooper()){
                ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                    if (loadingDialog!=null&&loadingDialog.isShowing()){
                        loadingDialog.dismiss();
                    }
                });
            }else if (loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
        }
    }


    public void setCommentAddListener(commentAddListener listener) {
        mListener=listener;
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }
}
