package com.applikey.mattermost.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applikey.mattermost.R;
import com.applikey.mattermost.adapters.channel.viewholder.GroupChatListViewHolder;
import com.applikey.mattermost.adapters.viewholders.ChatListViewHolder;
import com.applikey.mattermost.adapters.viewholders.ClickableViewHolder;
import com.applikey.mattermost.adapters.viewholders.MessageChannelViewHolder;
import com.applikey.mattermost.adapters.viewholders.UserViewHolder;
import com.applikey.mattermost.models.SearchItem;
import com.applikey.mattermost.models.channel.Channel;
import com.applikey.mattermost.models.post.Message;
import com.applikey.mattermost.models.user.User;
import com.applikey.mattermost.utils.RecyclerItemClickListener;
import com.applikey.mattermost.web.images.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RecyclerItemClickListener.OnItemClickListener {

    private List<SearchItem> mDataSet = new ArrayList<>();

    private ImageLoader mImageLoader;

    private ClickListener mClickListener;

    private String mCurrentUserId;

    public SearchAdapter(ImageLoader imageLoader, String currentUserId) {
        mCurrentUserId = currentUserId;
        mImageLoader = imageLoader;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      @SearchItem.Type int viewType) {
        final View view;
        final ClickableViewHolder viewHolder;

        if (viewType == SearchItem.CHANNEL) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_group_chat, parent, false);
            viewHolder = new GroupChatListViewHolder(view, mCurrentUserId);
        } else if (viewType == SearchItem.MESSAGE_CHANNEL) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_group_chat, parent, false);
            viewHolder = new MessageChannelViewHolder(view, mCurrentUserId);
        } else if (viewType == SearchItem.USER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_search_user, parent, false);
            viewHolder = new UserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_chat, parent, false);
            viewHolder = new ChatListViewHolder(view, mCurrentUserId);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {

        final int searchType = mDataSet.get(position).getSearchType();

        if (searchType == SearchItem.CHANNEL) {
            GroupChatListViewHolder viewHolder = (GroupChatListViewHolder) vh;
            viewHolder.bind(mImageLoader, (Channel) mDataSet.get(position));
            viewHolder.setClickListener(this);
        } else if (searchType == SearchItem.USER) {
            ((UserViewHolder) vh).bind(mImageLoader, this, (User) mDataSet.get(position));
        } else if (searchType == SearchItem.MESSAGE) {
            ((ChatListViewHolder) vh).bind(mImageLoader, this, (Message) mDataSet.get(position));
        } else if (searchType == SearchItem.MESSAGE_CHANNEL) {
            ((MessageChannelViewHolder) vh).bind(mImageLoader, this,
                                                 (Message) mDataSet.get(position));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position).getSearchType();
    }

    @Override
    public int getItemCount() {
        return mDataSet != null ? mDataSet.size() : 0;
    }

    public void setDataSet(List<SearchItem> dataSet) {
        mDataSet.clear();
        for (SearchItem searchItem : dataSet) {
            mDataSet.add(searchItem);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public void setOnClickListener(ClickListener listener) {
        this.mClickListener = listener;
    }

    @Override
    public void onItemClick(View childView, int position) {
        mClickListener.onItemClicked(mDataSet.get(position));
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    public interface ClickListener {
        void onItemClicked(SearchItem searchItem);
    }

}
