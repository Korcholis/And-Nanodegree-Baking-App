package com.korcholis.bakingapp.provider;

import android.content.Context;
import android.support.annotation.NonNull;

import com.korcholis.bakingapp.exceptions.ConnectionNotAvailableException;
import com.korcholis.bakingapp.utils.ConnectionChecker;
import com.korcholis.bakingapp.utils.Constants;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipesApi {
    private static final String BASE_URL = "http://go.udacity.com/";

    private static Retrofit instance = null;
    private RecipesApiSignature signature;

    @Inject
    public RecipesApi(Context context) {
        signature = instance(context);
    }

    public static RecipesApiSignature instance(final Context context) {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(createOkHttpClient(context))
                    .build();
        }

        return instance.create(RecipesApiSignature.class);
    }

    public static OkHttpClient createOkHttpClient(final Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), Constants.CACHE_DIR);
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        final OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {

            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                final Request original = chain.request();
                final HttpUrl originalHttpUrl = original.url();
                final Request.Builder requestBuilder = original.newBuilder()
                        .url(originalHttpUrl);
                final Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        }).addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                if (ConnectionChecker.isNetworkAvailable(context)) {
                    int maxAge = 60; // read from cache for 1 minute
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .build();
                } else {
                    int maxStale = 60 * 60 * 24 * 7; // tolerate 1-week stale
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .build();
                }
            }
        }).addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (ConnectionChecker.isNetworkAvailable(context)) {
                    return chain.proceed(chain.request());
                } else {
                    throw new ConnectionNotAvailableException();
                }
            }
        }).cache(cache);

        return httpClient.build();
    }

    public RecipesApiSignature api() {
        return signature;
    }
}
