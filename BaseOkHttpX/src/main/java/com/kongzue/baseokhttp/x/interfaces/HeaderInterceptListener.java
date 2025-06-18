package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;
import com.kongzue.baseokhttp.x.util.Parameter;

public interface HeaderInterceptListener {
    Parameter onIntercept(BaseHttpRequest httpRequest, String url, Parameter originHeader);
}
