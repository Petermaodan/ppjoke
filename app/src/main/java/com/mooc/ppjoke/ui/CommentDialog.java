package com.mooc.ppjoke.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.mooc.libcommon.utils.PixUtils;
import com.mooc.libcommon.view.ViewHelper;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.LayoutCommentDialogBinding;
import com.mooc.ppjoke.model.Comment;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {


    private static final String KEY_ITEM_ID = "key_item_id";
    //评论binding文件
    private LayoutCommentDialogBinding mBinding;
    private long itemId;

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
            CaptureActivity
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

    public void setCommentAddListener(commentAddListener listener) {
        mListener=listener;
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }
}
