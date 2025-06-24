package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import okhttp3.ResponseBody;

public interface BaseResponseListener {

    void response(BaseHttpRequest httpRequest, ResponseBody responseBody, Exception error);

}
