package com.kongzue.baseokhttp.x;

import com.kongzue.baseokhttp.x.interfaces.BaseResponseInterceptListener;
import com.kongzue.baseokhttp.x.interfaces.HeaderInterceptListener;
import com.kongzue.baseokhttp.x.interfaces.ParameterInterceptListener;
import com.kongzue.baseokhttp.x.interfaces.ResponseInterceptListener;
import com.kongzue.baseokhttp.x.util.Parameter;

import java.util.HashMap;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * BaseOkHttpX 全局配置类。
 */
public class BaseOkHttpX {

    // 服务器 URL
    public static String serviceUrl;

    // 是否调试模式
    public static boolean debugMode = true;

    // 超时时长（单位：秒）
    public static int globalTimeOutDuration = 10;

    // 强制验证放置在 SSL 证书（当值不为空时生效）
    public static String forceValidationOfSSLCertificatesFilePath;

    // 缓存请求: new Cache(path, cacheSize)
    public static Cache requestCacheSettings = null;

    // 容灾地址
    public static String[] reserveServiceUrls;

    // 保留 Cookies
    public static boolean keepCookies;

    // 已存储的 Cookie
    public static HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

    // 启用详细请求事件日志
    public static boolean httpRequestDetailsLogs = false;

    // 全局拦截器
    public static BaseResponseInterceptListener responseInterceptListener;

    // 全局参数拦截器
    public static ParameterInterceptListener parameterInterceptListener;

    // 全局Header拦截器
    public static HeaderInterceptListener headerInterceptListener;

    // 禁止重复请求
    public static boolean disallowSameRequest = false;

    // 全局 Header
    public static Parameter globalHeader;

    // 全局请求参数
    public static Parameter globalParameter;

    //ToDo: WebSocket
}
