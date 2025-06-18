package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.x.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public abstract class JsonResponseListener implements ResponseListener {

    @Override
    public void response(BaseHttpRequest httpRequest, String response, Exception error) {
        if (error == null) {
            JsonMap data = new JsonMap(response);
            if (!data.isEmpty()) {
                onResponse(httpRequest, data, null);
            } else {
                onResponse(httpRequest, new JsonMap(), new DecodeJsonException(response));
            }
        } else {
            onResponse(httpRequest, new JsonMap(), error);
        }
    }

    public abstract void onResponse(BaseHttpRequest httpRequest, JsonMap main, Exception error);
}
