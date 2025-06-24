package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import java.io.IOException;

import okhttp3.ResponseBody;

public abstract class BytesResponseInterceptListener implements BaseResponseInterceptListener {

    @Override
    @Deprecated
    public boolean onIntercept(BaseHttpRequest httpRequest, ResponseBody response, Exception error) {
        try {
            byte[] result = response.bytes();
            return onIntercept(httpRequest, result, error);
        } catch (IOException e) {
            return onIntercept(httpRequest, new byte[]{}, e);
        }
    }

    public abstract boolean onIntercept(BaseHttpRequest httpRequest, byte[] response, Exception error);
}
