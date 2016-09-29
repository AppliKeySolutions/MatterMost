package com.applikey.mattermost.web.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.applikey.mattermost.utils.rx.RetryWithDelay;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.schedulers.Schedulers;

public class PicassoImageLoader implements ImageLoader {

    private final Picasso picasso;

    public PicassoImageLoader(Context context, OkHttpClient client) {
        picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(client)).build();
    }

    @Override
    public Observable<Bitmap> getBitmapObservable(@NonNull String url) {
        return Observable.fromCallable(() -> picasso.load(url).get())
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(5, 300));
    }

    @Override
    public void displayImage(@NonNull String url, @NonNull ImageView imageView) {
        picasso.load(url).into(imageView);
    }

    @Override
    public void dropMemoryCache() {
        PicassoTools.clearCache(picasso);
    }
}
