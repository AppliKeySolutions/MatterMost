package com.applikey.mattermost.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.applikey.mattermost.R;
import com.applikey.mattermost.adapters.SearchAdapter;
import com.applikey.mattermost.models.SearchItem;
import com.applikey.mattermost.models.channel.Channel;
import com.applikey.mattermost.mvp.presenters.SearchChannelPresenter;
import com.applikey.mattermost.mvp.views.SearchChannelView;
import com.arellomobile.mvp.presenter.InjectPresenter;

import butterknife.Bind;

public class SearchMessageFragment extends SearchFragment implements SearchChannelView,
        SearchAdapter.ClickListener {

    @InjectPresenter
    SearchChannelPresenter mPresenter;

    @Bind(R.id.rv_items)
    RecyclerView mRecycleView;

    public static SearchMessageFragment newInstance() {
        return new SearchMessageFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(this);
        mPresenter.requestNotJoinedChannels();
        mPresenter.getData("");
    }

    @Override
    public void onItemClicked(SearchItem item) {
        mPresenter.handleChannelClick((Channel) item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenter.unSubscribe();
    }

    @Override
    public void startChatView(Channel channel) {
        // TODO: IMPLEMENT
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_search_chat;
    }

}
