package com.kongzue.baseokhttp.x;

import android.content.Context;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public class Post extends BaseHttpRequest {

    public Post(String url) {
        this.requestType = REQUEST_TYPE.POST;
        this.url = url;
    }

    public Post(Context context, String url) {
        this.requestType = REQUEST_TYPE.POST;
        this.url = url;
        this.bindLifecycleOwner(context);
    }

    public static Post postRequest(String url) {
        return new Post(url);
    }

    public static Post postRequest(Context context, String url) {
        return new Post(context, url);
    }

    public static Post create(String url) {
        return new Post(url);
    }

    public static Post create(Context context, String url) {
        return new Post(context, url);
    }

}
