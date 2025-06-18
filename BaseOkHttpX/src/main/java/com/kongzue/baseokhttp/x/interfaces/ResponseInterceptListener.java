package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public interface ResponseInterceptListener {

    boolean onIntercept(BaseHttpRequest httpRequest, String response, Exception error);
}
