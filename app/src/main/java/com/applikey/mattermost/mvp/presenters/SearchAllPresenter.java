package com.applikey.mattermost.mvp.presenters;

import android.text.TextUtils;

import com.applikey.mattermost.App;
import com.applikey.mattermost.Constants;
import com.applikey.mattermost.events.SearchAllTextChanged;
import com.applikey.mattermost.models.SearchItem;
import com.applikey.mattermost.models.channel.Channel;
import com.applikey.mattermost.models.post.Message;
import com.applikey.mattermost.models.post.Post;
import com.applikey.mattermost.models.post.PostResponse;
import com.applikey.mattermost.models.post.PostSearchRequest;
import com.applikey.mattermost.models.user.User;
import com.applikey.mattermost.mvp.views.SearchAllView;
import com.applikey.mattermost.storage.db.ChannelStorage;
import com.applikey.mattermost.storage.db.UserStorage;
import com.applikey.mattermost.storage.preferences.Prefs;
import com.applikey.mattermost.web.ErrorHandler;
import com.arellomobile.mvp.InjectViewState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

@InjectViewState
public class SearchAllPresenter extends SearchPresenter<SearchAllView> {

    private static final String TAG = SearchAllPresenter.class.getSimpleName();

    @Inject
    ChannelStorage mChannelStorage;

    @Inject
    UserStorage mUserStorage;

    @Inject
    EventBus mEventBus;

    @Inject
    @Named(Constants.CURRENT_USER_QUALIFIER)
    String mCurrentUserId;

    @Inject
    Prefs mPrefs;

    @Inject
    ErrorHandler mErrorHandler;

    public SearchAllPresenter() {
        App.getUserComponent().inject(this);
        mEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
    }

    public void getData(String text) {
        if (!mChannelsIsFetched  || TextUtils.isEmpty(text)) {
            return;
        }
        final SearchAllView view = getViewState();
        view.setEmptyState(true);
        mSubscription.clear();


        final Observable<List<SearchItem>> postItems =
                mApi.searchPosts(mPrefs.getCurrentTeamId(), new PostSearchRequest(text))
                .map(PostResponse::getPosts)
                        .filter(postMap -> postMap != null)
                .map(Map::values)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Observable::from)
                .flatMap(item -> mChannelStorage.channelById(item.getChannelId()).first(),
                         (Func2<Post, Channel, SearchItem>) Message::new)
                .toList();

        mSubscription.add(
                Observable.zip(
                        Channel.getList(mChannelStorage.listUndirected(text))
                                .doOnNext(channels -> addFilterChannels(channels, text)),
                        mUserStorage.searchUsers(text), (items, users) -> {

                            final List<SearchItem> searchItemList = new ArrayList<>();

                            for (Channel item : items) {
                                searchItemList.add(item);
                            }
                            for (User user : users) {
                                searchItemList.add(user);
                            }

                            return searchItemList;
                        })
                        .flatMap(items -> postItems,
                                 (items, items2) -> {
                                     items.addAll(items2);
                                     return items;
                                 })
                        .debounce(Constants.INPUT_REQUEST_TIMEOUT_MILLISEC, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::displayData, mErrorHandler::handleError));
    }

    @Subscribe
    public void onInputTextChanged(SearchAllTextChanged event) {
        final SearchAllView view = getViewState();
        view.clearData();
        getData(event.getText());
    }


}
