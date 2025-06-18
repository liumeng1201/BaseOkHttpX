package com.kongzue.baseokhttp.x.interfaces;

import android.content.Context;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/11/22 17:27
 */
public interface ParameterInterceptListener {
    Object onIntercept(BaseHttpRequest httpRequest, String url, Object parameter);
}
