package com.applikey.mattermost.mvp.presenters;

import com.applikey.mattermost.App;
import com.applikey.mattermost.mvp.views.RestorePasswordView;
import com.applikey.mattermost.utils.kissUtils.utils.StringUtil;
import com.applikey.mattermost.web.Api;
import com.applikey.mattermost.web.ErrorHandler;
import com.arellomobile.mvp.InjectViewState;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;

@InjectViewState
public class RestorePasswordPresenter extends BasePresenter<RestorePasswordView> {

    private static final String INVALID_EMAIL_MESSAGE = "Invalid Email";

    @Inject
    Api mApi;

    public RestorePasswordPresenter() {
        App.getComponent().inject(this);
    }

    public void sendRestorePasswordRequest(String email) {
        final RestorePasswordView view = getViewState();

        if (!validateEmailFormat(email)) {
            view.onFailure(INVALID_EMAIL_MESSAGE);
            return;
        }

        mSubscription.add(mApi.sendPasswordReset(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    view.onPasswordRestoreSent();
                }, throwable -> {
                    ErrorHandler.handleError(throwable);
                    view.onFailure(throwable.getMessage());
                }));
    }

    private boolean validateEmailFormat(String email) {
        return !email.trim().isEmpty() && StringUtil.isEmail(email);
    }
}