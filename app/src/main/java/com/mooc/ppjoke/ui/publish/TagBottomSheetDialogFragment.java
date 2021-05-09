package com.mooc.ppjoke.ui.publish;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mooc.libcommon.utils.PixUtils;
import com.mooc.libnetwork.ApiResponse;
import com.mooc.libnetwork.ApiService;
import com.mooc.libnetwork.JsonCallback;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.model.TagList;
import com.mooc.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.List;

public class TagBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private List<TagList> mTagLists=new ArrayList<>();
    private OnTagItemSelectedListener listener;
    private TagsAdapter tagsAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog=super.onCreateDialog(savedInstanceState);
        View view= LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_botton_sheet_dialog,null,false);
        recyclerView=view.findViewById(R.id.record_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tagsAdapter=new TagsAdapter();
        recyclerView.setAdapter(tagsAdapter);

        //用setContentView给其设置一个View，这样才能看到展开的效果。
        dialog.setContentView(view);
        //View使用setContentView之后会进行一次包装，所以之后的View应该是获取他的ParentView，之后behavior设置其对应的高度。
        ViewGroup parent= (ViewGroup) view.getParent();
        BottomSheetBehavior<ViewGroup> behavior=BottomSheetBehavior.from(parent);
        behavior.setPeekHeight(PixUtils.getScreenHeight()/3);
        behavior.setHideable(false);

        ViewGroup.LayoutParams layoutParams=parent.getLayoutParams();
        layoutParams.height=PixUtils.getScreenHeight()/3*2;
        parent.setLayoutParams(layoutParams);

        //进行服务器数据请求
        queryTagList();
        return dialog;
    }

    private void queryTagList() {
        ApiService.get("/tag/queryTagList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount",100)
                .addParam("tagId",0).execute(new JsonCallback<List<TagList>>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(ApiResponse<List<TagList>> response) {
                super.onSuccess(response);
                if (response.body!=null){
                    List<TagList> list=response.body;
                    mTagLists.addAll(list);
                    ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                        tagsAdapter.notifyDataSetChanged();
                    });
                }
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onError(ApiResponse<List<TagList>> response) {
                ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                    Toast.makeText(getContext(), response.message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void setOnTagItemSelectedListener(OnTagItemSelectedListener listener) {
        this.listener=listener;
    }

    class TagsAdapter extends RecyclerView.Adapter {
         @NonNull
         @Override
         public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
             TextView textView=new TextView(parent.getContext());
             textView.setTextSize(13);
             textView.setTypeface(Typeface.DEFAULT_BOLD);
             textView.setGravity(Gravity.CENTER_VERTICAL);
             textView.setLayoutParams(new RecyclerView.LayoutParams(-1, PixUtils.dp2px(45)));

             return new RecyclerView.ViewHolder(textView){};

         }

         @Override
         public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
             TextView textView= (TextView) holder.itemView;
             TagList tagList=mTagLists.get(position);
             textView.setText(tagList.title);
             holder.itemView.setOnClickListener(new View.OnClickListener(){

                 @Override
                 public void onClick(View view) {
                     if (listener!=null){
                         listener.onTagItemSelected(tagList);
                         dismiss();
                     }
                 }
             });
         }

         @Override
         public int getItemCount() {
             return mTagLists.size();
         }
     }

    public interface OnTagItemSelectedListener {
        void onTagItemSelected(TagList tagList);
    }
}
