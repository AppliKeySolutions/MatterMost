package com.applikey.mattermost.mvp.presenters;

import com.applikey.mattermost.App;
import com.applikey.mattermost.models.post.Post;
import com.applikey.mattermost.models.post.PostDto;
import com.applikey.mattermost.models.post.PostResponse;
import com.applikey.mattermost.models.user.User;
import com.applikey.mattermost.mvp.views.ChatView;
import com.applikey.mattermost.storage.db.PostStorage;
import com.applikey.mattermost.storage.db.TeamStorage;
import com.applikey.mattermost.storage.db.UserStorage;
import com.applikey.mattermost.web.Api;
import com.applikey.mattermost.web.ErrorHandler;
import com.arellomobile.mvp.InjectViewState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@InjectViewState
public class ChatPresenter extends BasePresenter<ChatView> {

    private static final int PAGE_SIZE = 60;

    @Inject
    PostStorage mPostStorage;

    @Inject
    TeamStorage mTeamStorage;

    @Inject
    UserStorage mUserStorage;

    @Inject
    Api mApi;

    public ChatPresenter() {
        App.getComponent().inject(this);
    }

    public void getInitialData(String channelId) {
        final ChatView view = getViewState();
        mSubscription.add(Observable.zip(
                mPostStorage.listByChannel(channelId),
                mUserStorage.listDirectProfiles(),
                this::transform)
                .subscribe(view::displayData,
                        view::onFailure));
    }

    public void fetchData(String channelId, int offset) {
        mSubscription.add(mTeamStorage.getChosenTeam()
                .flatMap(team ->
                        mApi.getPostsPage(team.getId(), channelId, offset, PAGE_SIZE)
                                .subscribeOn(Schedulers.io())
                                .doOnError(ErrorHandler::handleError))
                .map(response -> transform(response, offset))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {
                    mPostStorage.saveAll(posts);
                    getViewState().onDataFetched();
                }, ErrorHandler::handleError));
    }

    private List<Post> transform(PostResponse response, int offset) {
        final List<String> order = response.getOrder();

        for (int i = 0; i < order.size(); i++) {
            final String id = order.get(i);

            response.getPosts().get(id).setPriority(i + offset);
        }

        return new ArrayList<>(response.getPosts().values());
    }

    private List<PostDto> transform(List<Post> posts, List<User> profiles) {
        final Map<String, User> userMap = new HashMap<>();
        for (User profile : profiles) {
            userMap.put(profile.getId(), profile);
        }

        final List<PostDto> result = new ArrayList<>(posts.size());

        for (Post post : posts) {
            final User profile = userMap.get(post.getUserId());
            final String userName = User.getDisplayableName(profile);
            final String userAvatar = profile.getProfileImage();
            final User.Status userStatus = User.Status.from(profile.getStatus());

            final PostDto dto = new PostDto(post, userName, userAvatar, userStatus);
            result.add(dto);
        }

        return result;
    }
}