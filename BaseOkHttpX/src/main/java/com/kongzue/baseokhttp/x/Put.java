package com.kongzue.baseokhttp.x;

import android.content.Context;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public class Put extends BaseHttpRequest {

    public Put(String url) {
        this.requestType = REQUEST_TYPE.PUT;
        this.url = url;
    }

    public Put(Context context, String url) {
        this.requestType = REQUEST_TYPE.PUT;
        this.url = url;
        this.bindLifecycleOwner(context);
    }

    public static Put putRequest(String url) {
        return new Put(url);
    }

    public static Put putRequest(Context context, String url) {
        return new Put(context, url);
    }

    public static Put create(String url) {
        return new Put(url);
    }

    public static Put create(Context context, String url) {
        return new Put(context, url);
    }
}
