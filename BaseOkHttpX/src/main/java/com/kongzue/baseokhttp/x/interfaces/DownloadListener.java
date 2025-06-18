package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import java.io.File;

public interface DownloadListener {

    void onDownload(BaseHttpRequest httpRequest, File downloadFile, float progress, long current, long total, boolean done);
}
