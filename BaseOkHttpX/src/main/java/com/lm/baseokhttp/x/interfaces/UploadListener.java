package com.lm.baseokhttp.x.interfaces;

import com.lm.baseokhttp.x.util.BaseHttpRequest;

/**
 * 上传进度回调接口。
 */
public interface UploadListener {

    /**
     * 上传进度回调
     *
     * @param httpRequest 当前请求对象
     * @param progress    进度（0~1）
     * @param current     已上传字节数
     * @param total       总字节数
     * @param done        是否完成
     */
    void onUpload(BaseHttpRequest httpRequest, float progress, long current, long total, boolean done);
}
