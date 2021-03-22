package com.mooc.ppjoke.ui.home;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;

import com.mooc.ppjoke.model.Feed;

public class FeedAdapter extends PagedListAdapter<Feed,FeedAdapter.ViewHolder> {
    protected FeedAdapter(@NonNull AsyncDifferConfig<Feed> config) {
        super(config);
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedAdapter.ViewHolder holder, int position) {

    }
}
