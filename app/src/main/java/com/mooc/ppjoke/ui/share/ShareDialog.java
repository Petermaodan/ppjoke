package com.mooc.ppjoke.ui.share;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mooc.libcommon.utils.PixUtils;
import com.mooc.libcommon.view.CornerFrameLayout;
import com.mooc.libcommon.view.ViewHelper;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.view.PPImageView;

import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {
    private CornerFrameLayout layout;
     List<ResolveInfo> shareitem=new ArrayList<>();
    private String shareContent;
    private View.OnClickListener mListener;
    private ShareAdapter shareAdapter;

    public ShareDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        layout=new CornerFrameLayout(getContext());
        layout.setBackgroundColor(Color.WHITE);
        layout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP);

        RecyclerView gridView=new RecyclerView(getContext());
        gridView.setLayoutManager(new GridLayoutManager(getContext(),4));//?

        //个ShareAdapter类之后对，其中的几个方法进行重写，继承自 RecyclerView.Adapter，用于缓存数据
        shareAdapter=new ShareAdapter();
        //设置过滤器
        gridView.setAdapter(shareAdapter);
        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin=PixUtils.dp2px(20);

        //这是边界距离,设置layout的属性
        params.leftMargin=params.topMargin=params.rightMargin=params.bottomMargin;
        params.gravity= Gravity.CENTER;
        layout.addView(gridView,params);

        setContentView(layout);
        getWindow().setGravity(Gravity.BOTTOM);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        //添加查询路口quertSharaItems()方法，把我们构造的Intent传入进去。通过List<ResolveInfo>对分享进行过滤，之后微信或者QQ的才能保留下来
        // ，最后shareAdapter对象调用notifyDataSetChanged()方法进行刷新即可。
        queryShareItems();
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public void setShareItemClickListener(View.OnClickListener mListener) {
        this.mListener = mListener;
    }

    private void queryShareItems() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        List<ResolveInfo> resolveInfos=getContext().getPackageManager().queryIntentActivities(intent,0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName=resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName,"com.tencent.mm")||TextUtils.equals(packageName,"com.tencent.mobileqq")){
                shareitem.add(resolveInfo);
            }
        }
        //最后shareAdapter对象调用notifyDataSetChanged()方法进行刷新即可。
        shareAdapter.notifyDataSetChanged();

    }

    private class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final PackageManager packageManager;

        public ShareAdapter() {
            packageManager=getContext().getPackageManager();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //创建一个layout_share_item的布局，其中的布局有两个，一个是PPImageView的布局，另一个是TextView的布局，来判断发送给谁。
            View inflate= LayoutInflater.from(getContext()).inflate(R.layout.layout_share_item,parent,false);
            return new RecyclerView.ViewHolder(inflate) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ResolveInfo resolveInfo=shareitem.get(position);
            PPImageView imageView=holder.itemView.findViewById(R.id.share_icon);
            Drawable drawable=resolveInfo.loadIcon(packageManager);
            imageView.setImageDrawable(drawable);

            TextView shareText=holder.itemView.findViewById(R.id.share_text);
            shareText.setText(resolveInfo.loadLabel(packageManager));

            //给holder.itemView中添加一个按钮响应，来唤醒响应的客户端
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pkg=resolveInfo.activityInfo.packageName;
                    String cls=resolveInfo.activityInfo.name;
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setComponent(new ComponentName(pkg,cls));
                    intent.putExtra(Intent.EXTRA_TEXT,shareContent);

                    getContext().startActivity(intent);
                    if (mListener!=null){
                        mListener.onClick(view);
                    }
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return shareitem==null?0:shareitem.size();
        }
    }
}
