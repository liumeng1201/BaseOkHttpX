package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import okhttp3.ResponseBody;

public abstract class BytesResponseListener implements BaseResponseListener {

    @Override
    @Deprecated
    public void response(BaseHttpRequest httpRequest, ResponseBody responseBody, Exception error) {
        try {
            byte[] data = responseBody.bytes();
            response(httpRequest, data, error);
        } catch (Exception e) {
            response(httpRequest, new byte[]{}, e);
        }
    }

    public abstract void response(BaseHttpRequest httpRequest, byte[] response, Exception error);
}
