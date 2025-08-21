package com.lm.baseokhttp.x.exceptions;

import com.lm.baseokhttp.x.util.RequestInfo;

public class SameRequestException extends Exception{

    public SameRequestException(RequestInfo requestInfo) {
        super("拦截重复请求:" + requestInfo);
    }
}
