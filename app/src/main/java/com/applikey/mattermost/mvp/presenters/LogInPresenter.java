package com.applikey.mattermost.mvp.presenters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.applikey.mattermost.App;
import com.applikey.mattermost.models.auth.AuthenticationRequest;
import com.applikey.mattermost.models.auth.AuthenticationResponse;
import com.applikey.mattermost.models.web.RequestError;
import com.applikey.mattermost.mvp.views.LogInView;
import com.applikey.mattermost.storage.TeamStorage;
import com.applikey.mattermost.storage.preferences.Prefs;
import com.applikey.mattermost.web.Api;
import com.applikey.mattermost.web.ErrorHandler;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Headers;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

// TODO unsubscribe composite subscription OR migrate to RxPresenter (like RxFragment and RxActivity)
public class LogInPresenter extends SingleViewPresenter<LogInView> {

    @Inject
    Api mApi;

    @Inject
    Prefs mPrefs;

    @Inject
    TeamStorage teamStorage;

    private CompositeSubscription mSubscription;

    public LogInPresenter() {
        App.getComponent().inject(this);

        mSubscription = new CompositeSubscription();
    }

    // TODO: pre-validation. NOTE: Use stringutils
    public void authorize(Activity context, String teamId, String email, String password) {
        final LogInView view = getView();
        final AuthenticationRequest request = new AuthenticationRequest(teamId, email, password);

        mSubscription.add(mApi.authorize(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInResponse -> {
                    // TODO Handle 4xx codes, etc
                    handleSuccessfulResponse(signInResponse);
                }, throwable -> {
                    ErrorHandler.handleError(context, throwable);
                    view.onUnsuccessfulAuth(throwable.getMessage());
                }));
    }

    public void getInitialData() {
        Log.d("LogInPresenter", "getInitialData");

        mSubscription.add(teamStorage.listAll()
                .subscribe(entries -> {
                    getView().displayTeams(entries);
                }));
    }

    public void unSubscribe() {
        mSubscription.unsubscribe();
    }

    private void handleSuccessfulResponse(Response<AuthenticationResponse> response) {
        final int code = response.code();

        final int codesGroup = code / 100;

        // Handle success
        if (codesGroup == 2) {
            cacheHeaders(response);
            getView().onSuccessfulAuth();
            return;
        }

        // Handle failure
        try {
            final String message = RequestError.fromJson(response.errorBody().string())
                    .getMessage();
            getView().onUnsuccessfulAuth(message);
            ErrorHandler.handleError(message);
        } catch (IOException e) {
            ErrorHandler.handleError(e);
        }
    }

    private void cacheHeaders(Response<AuthenticationResponse> response) {
        final Headers headers = response.headers();
        final String authenticationToken = headers.get("token");

        mPrefs.setKeyAuthToken(authenticationToken);
    }
}
