package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public interface ResponseListener {

    void response(BaseHttpRequest httpRequest, String response, Exception error);
}
