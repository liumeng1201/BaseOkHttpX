package com.kongzue.baseokhttp.x;

import android.content.Context;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

/**
 * DELETE 请求封装类。
 */
public class Delete extends BaseHttpRequest {

    public Delete(String url) {
        this.requestType = REQUEST_TYPE.DELETE;
        this.url = url;
    }

    public Delete(Context context, String url) {
        this.requestType = REQUEST_TYPE.DELETE;
        this.url = url;
        this.bindLifecycleOwner(context);
    }

    public static Delete deleteRequest(String url) {
        return new Delete(url);
    }

    public static Delete deleteRequest(Context context, String url) {
        return new Delete(context, url);
    }

    public static Delete create(String url) {
        return new Delete(url);
    }

    public static Delete create(Context context, String url) {
        return new Delete(context, url);
    }
}
