package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

public interface UploadListener {

    void onUpload(BaseHttpRequest httpRequest, float progress, long current, long total, boolean done);
}
