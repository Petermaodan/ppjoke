package com.mooc.ppjoke.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mooc.ppjoke.BR;
import com.mooc.ppjoke.databinding.LayoutFeedTypeImageBinding;
import com.mooc.ppjoke.databinding.LayoutFeedTypeVideoBinding;
import com.mooc.ppjoke.model.Feed;
import com.mooc.ppjoke.view.ListPlayerView;

import java.util.Date;

public class FeedAdapter extends PagedListAdapter<Feed,FeedAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    protected Context mContext;
    protected String mCategory;

    public FeedAdapter(Context context,String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id==newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        inflater=LayoutInflater.from(context);
        mContext=context;
        mCategory=category;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding=null;
        if (viewType==Feed.TYPE_IMAGE){
            binding=LayoutFeedTypeImageBinding.inflate(inflater);
        }else {
            binding=LayoutFeedTypeVideoBinding.inflate(inflater);
        }
        return new ViewHolder(binding.getRoot(),binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedAdapter.ViewHolder holder, int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;
        public ImageView feedImage;


        public ViewHolder(@NonNull View itemView,ViewDataBinding binding) {
            super(itemView);
            mBinding=binding;
        }
        public void bindData(Feed item){
            //这里之所以手动绑定数据的原因是 图片 和视频区域都是需要计算的
            //而dataBinding的执行默认是延迟一帧的。
            //当列表上下滑动的时候 ，会明显的看到宽高尺寸不对称的问题

            if (mBinding instanceof LayoutFeedTypeImageBinding){
                LayoutFeedTypeImageBinding imageBinding= (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.width,item.height,16,item.cover);
            }else {
                LayoutFeedTypeVideoBinding videoBinding= (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setFeed(item);
                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(mCategory,item.width,item.height,item.cover,item.url);
            }
        }

        public boolean isVideoItem(){
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }
    }
}
