package com.kongzue.baseokhttp.x.interfaces;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import java.io.InputStream;

import okhttp3.ResponseBody;

public abstract class BitmapResponseListener implements BaseResponseListener {

    @Override
    @Deprecated
    public void response(BaseHttpRequest httpRequest, ResponseBody responseBody, Exception error) {
        try {
            InputStream inputStream = responseBody.byteStream();
            response(httpRequest, BitmapFactory.decodeStream(inputStream), error);
        } catch (Exception e) {
            response(httpRequest, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), e);
        }
    }

    public abstract void response(BaseHttpRequest httpRequest, Bitmap response, Exception error);
}