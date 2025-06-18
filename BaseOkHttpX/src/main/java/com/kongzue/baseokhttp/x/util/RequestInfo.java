package com.kongzue.baseokhttp.x.util;

import android.util.Log;

import com.kongzue.baseokhttp.x.BaseOkHttpX;
import com.kongzue.baseokhttp.x.interfaces.ResponseListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/11/4 12:48
 */
public class RequestInfo {

    private String url;
    private String parameter;

    private List<ResponseListener> sameRequestCallbacks = new ArrayList<ResponseListener>();

    public RequestInfo(String url, String parameter) {
        this.url = url;
        this.parameter = parameter;
    }

    public RequestInfo(String url, Parameter parameter) {
        this.url = url;
        this.parameter = parameter == null ? "" : parameter.toParameterString();
    }

    public String getUrl() {
        return url;
    }

    public RequestInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getParameter() {
        return parameter;
    }

    public RequestInfo setParameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    public boolean equals(RequestInfo requestInfo) {
        if (this == requestInfo) return true;
        if (requestInfo == null || getClass() != requestInfo.getClass()) return false;
        return equalsString(url, requestInfo.url) && equalsString(parameter, requestInfo.parameter);
    }

    private boolean equalsString(String a, String b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public void addSameRequestCallback(ResponseListener listener) {
        sameRequestCallbacks.add(listener);
    }

    public List<ResponseListener> getSameRequestCallbacks() {
        return sameRequestCallbacks;
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "url='" + url + '\'' +
                ", parameter='" + parameter + '\'' +
                '}';
    }

    public static void cleanSameRequestList() {
        requestInfoList = new ArrayList<>();
    }

    private static List<RequestInfo> requestInfoList;

    public static void addRequestInfo(RequestInfo requestInfo) {
        synchronized (RequestInfo.class) {
            if (requestInfoList == null) {
                requestInfoList = new ArrayList<>();
            }
            requestInfoList.add(requestInfo);
        }
    }

    public static void deleteRequestInfo(RequestInfo requestInfo) {
        synchronized (RequestInfo.class) {
            if (requestInfoList == null || requestInfoList.isEmpty() || requestInfo == null) {
                return;
            }
            requestInfoList.remove(requestInfo);
        }
    }

    public static RequestInfo equalsRequestInfo(RequestInfo requestInfo) {
        synchronized (RequestInfo.class) {
            if (requestInfoList == null || requestInfoList.isEmpty()) {
                return null;
            }
            for (RequestInfo requestInfo1 : requestInfoList) {
                if (requestInfo1.equals(requestInfo)) {
                    return requestInfo1;
                }
            }
            return null;
        }
    }
}
