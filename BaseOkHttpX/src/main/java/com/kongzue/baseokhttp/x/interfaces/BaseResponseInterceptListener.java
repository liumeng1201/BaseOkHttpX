package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import okhttp3.ResponseBody;

public interface BaseResponseInterceptListener {

    boolean onIntercept(BaseHttpRequest httpRequest, ResponseBody response, Exception error);
}
