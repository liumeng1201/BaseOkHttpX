package com.kongzue.baseokhttp.x;

import android.content.Context;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

/**
 * PATCH 请求封装类。
 */
public class Patch extends BaseHttpRequest {

    public Patch(String url) {
        this.requestType = REQUEST_TYPE.PATCH;
        this.url = url;
    }

    public Patch(Context context, String url) {
        this.requestType = REQUEST_TYPE.PATCH;
        this.url = url;
        this.bindLifecycleOwner(context);
    }

    public static Patch patchRequest(String url) {
        return new Patch(url);
    }

    public static Patch patchRequest(Context context, String url) {
        return new Patch(context, url);
    }

    public static Patch create(String url) {
        return new Patch(url);
    }

    public static Patch create(Context context, String url) {
        return new Patch(context, url);
    }
}
