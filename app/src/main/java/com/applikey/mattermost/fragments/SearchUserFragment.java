package com.applikey.mattermost.fragments;

import android.os.Bundle;
import android.view.View;

import com.applikey.mattermost.adapters.SearchAdapter;
import com.applikey.mattermost.models.SearchItem;
import com.applikey.mattermost.models.user.User;
import com.applikey.mattermost.mvp.presenters.SearchUserPresenter;
import com.applikey.mattermost.mvp.views.SearchUserView;
import com.arellomobile.mvp.presenter.InjectPresenter;

public class SearchUserFragment extends SearchFragment implements SearchUserView,
        SearchAdapter.ClickListener {

    @InjectPresenter
    SearchUserPresenter mPresenter;

    public static SearchUserFragment newInstance() {
        return new SearchUserFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(this);
        mPresenter.getData("");
    }

    @Override
    public void onItemClicked(SearchItem item) {
        mPresenter.handleUserClick((User) item);
    }

    @Override
    public void displayData(List<User> users) {
        mUserAdapter.setDataSet(users);
    }

    @Override
    public void clearData() {
        mUserAdapter.clear();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_search_chat;
    }

    private void initView() {
        mUserAdapter = new UserAdapter(mImageLoader);
        mUserAdapter.setOnClickListener(this);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleView.setAdapter(mUserAdapter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenter.unSubscribe();
    }

}
