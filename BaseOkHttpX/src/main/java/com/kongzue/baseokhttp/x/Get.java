package com.kongzue.baseokhttp.x;

import android.content.Context;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public class Get extends BaseHttpRequest {

    public Get(String url) {
        this.requestType = REQUEST_TYPE.GET;
        this.url = url;
    }

    public Get(Context context, String url) {
        this.requestType = REQUEST_TYPE.GET;
        this.url = url;
        this.bindLifecycleOwner(context);
    }

    public static Get getRequest(String url) {
        return new Get(url);
    }

    public static Get getRequest(Context context, String url) {
        return new Get(context, url);
    }

    public static Get create(String url) {
        return new Get(url);
    }

    public static Get create(Context context, String url) {
        return new Get(context, url);
    }
}
