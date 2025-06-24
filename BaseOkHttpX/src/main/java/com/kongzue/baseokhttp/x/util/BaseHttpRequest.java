package com.kongzue.baseokhttp.x.util;

import static com.kongzue.baseokhttp.x.util.LockLog.formatJson;
import static com.kongzue.baseokhttp.x.util.LockLog.getExceptionInfo;
import static com.kongzue.baseokhttp.x.util.RequestInfo.addRequestInfo;
import static com.kongzue.baseokhttp.x.util.RequestInfo.deleteRequestInfo;
import static com.kongzue.baseokhttp.x.util.RequestInfo.equalsRequestInfo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.x.BaseOkHttpX;
import com.kongzue.baseokhttp.x.exceptions.RequestException;
import com.kongzue.baseokhttp.x.exceptions.TimeOutException;
import com.kongzue.baseokhttp.x.interfaces.BaseResponseListener;
import com.kongzue.baseokhttp.x.interfaces.DownloadListener;
import com.kongzue.baseokhttp.x.interfaces.UploadListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BaseHttpRequest {

    public enum REQUEST_TYPE {
        GET, POST, PUT, DELETE, PATCH
    }

    public enum REQUEST_BODY_TYPE {
        STRING, JSON, FORM, FILE
    }

    protected REQUEST_TYPE requestType = REQUEST_TYPE.GET;      //请求方式
    protected REQUEST_BODY_TYPE requestBodyType;                //请求体类型

    protected String url;
    protected List<BaseResponseListener> callbacks = new ArrayList<>();
    protected Proxy proxy;
    protected OkHttpClient okHttpClient;
    protected long timeoutDuration = BaseOkHttpX.globalTimeOutDuration;     //请求超时（秒）
    protected boolean callbackInMainLooper;                     //强行主线程回调
    protected boolean callAsync;                                //直接在当前线程请求
    protected String requestMimeType = "text/plain; charset=utf-8";

    protected Parameter headerParameter = new Parameter(BaseOkHttpX.globalHeader);
    protected Parameter requestParameter = new Parameter(BaseOkHttpX.globalParameter);
    protected String stringRequestParameter;
    private final Cache undefinedCache = new Cache(new File(""), 1);
    protected Cache cacheSettings = undefinedCache;
    protected File downloadFile;
    protected UploadListener uploadListener;
    protected DownloadListener downloadListener;
    protected boolean showLogs = true;
    protected String cookieStr;
    protected boolean streamRequest;                            //流式请求

    protected boolean requesting;

    public void go(BaseResponseListener callback) {
        registerCallback(callback);
        go();
    }

    protected BaseHttpRequest() {
    }

    private Call httpCall;
    private Handler handler;

    public void go() {
        if (isShowLogs()) {
            LockLog.Builder logBuilder = LockLog.Builder.create()
                    .i(">>>", "-------------------------------------")
                    .i(">>>", "发出" + requestType.name() + "请求:" + getUrl() + " 请求时间：" + getNowTimeStr());
            if (!getHeaderParameter().isEmpty()) {
                logBuilder.i(">>>", "请求头:\t" + getHeaderParameter());
            }
            if (requestBodyType != null) {
                logBuilder.i(">>>", requestBodyType.name() + "参数:\n" + formatParameterStr());
            }
            logBuilder.i(">>>", "=====================================")
                    .build();

        }
        OkHttpClient client = okHttpClient == null ? createClient() : okHttpClient;
        Request request = createRequest();
        httpCall = client.newCall(request);
        if (handler == null) {
            Looper myLooper = Looper.myLooper();
            handler = myLooper == null ? null : new Handler(myLooper);
        }
        requestInfo = new RequestInfo(url, getRequestParameter());
        if (BaseOkHttpX.disallowSameRequest && equalsRequestInfo(requestInfo) != null) {
            LockLog.logE("<<<", "拦截重复请求:" + requestInfo);
            return;
        }
        addRequestInfo(requestInfo);
        setRequesting(true);
        if (callAsync) {
            try (Response response = httpCall.execute()) {
                onFinish(response);
            } catch (Exception e) {
                onFail(e);
            }
        } else {
            httpCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setRequesting(false);
                    onFail(e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (isStreamRequest()) {
                            if (!response.isSuccessful()) {
                                onFail(new RequestException(call, response.code()));
                                return;
                            }
                            try (ResponseBody responseBody = response.body();
                                 BufferedReader reader = new BufferedReader(
                                         new InputStreamReader(responseBody.byteStream()))) {
                                if (isShowLogs()) {
                                    LockLog.Builder logBuilder = LockLog.Builder.create()
                                            .i("<<<", "-------------------------------------")
                                            .i("<<<", "成功" + requestType.name() + "请求:" + getUrl() + " 返回时间：" + getNowTimeStr());
                                    if (requestBodyType != null) {
                                        logBuilder.i("<<<", requestBodyType.name() + "参数:\n" + formatParameterStr());
                                    }
                                    logBuilder.i("<<<", "返回内容:");
                                    logBuilder.build();
                                }
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    onStream(line, responseBody.contentType());
                                }
                                if (isShowLogs()) {
                                    LockLog.logI("<<<", "=====================================");
                                }
                                setRequesting(false);
                            }
                        } else {
                            setRequesting(false);
                            onFinish(response);
                        }
                    } finally {
                        response.close();
                    }
                }
            });
        }
    }

    private String formatParameterStr() {
        if (BaseOkHttpX.parameterInterceptListener != null) {
            Object parameter = BaseOkHttpX.parameterInterceptListener.onIntercept(BaseHttpRequest.this, getUrl(), getRequestParameter());
            return parameter instanceof Parameter ? ((Parameter) parameter).toString(requestBodyType) : String.valueOf(parameter);
        } else {
            return requestBodyType == REQUEST_BODY_TYPE.STRING ? stringRequestParameter : getRequestParameter().toString(requestBodyType);
        }
    }

    private void onStream(String line, MediaType mediaType) {
        deleteRequestInfo(requestInfo);
        if (isShowLogs()) {
            LockLog.logI("<<<", line);
        }
        ResponseBody responseBody = ResponseBody.create(line.getBytes(), mediaType);
        if (callbackInMainLooper) {
            Looper mainLooper = Looper.getMainLooper();
            handler = new Handler(mainLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callCallbacks(responseBody, null);
                }
            });
        } else {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callCallbacks(responseBody, null);
                    }
                });
            } else {
                callCallbacks(responseBody, null);
            }
        }
    }

    private void onFinish(Response response) {
        deleteRequestInfo(requestInfo);
        try (Response r = response) {
            if (downloadFile == null) {
                ResponseBody body = r.body();
                byte[] responseBytes = body.bytes();
                MediaType mediaType = body.contentType();
                String charset = mediaType.charset(StandardCharsets.UTF_8).name();
                String result = new String(responseBytes, charset);
                ResponseBody resultBody = ResponseBody.create(responseBytes, mediaType);

                if (isShowLogs()) {
                    LockLog.Builder logBuilder = LockLog.Builder.create()
                            .i("<<<", "-------------------------------------")
                            .i("<<<", "成功" + requestType.name() + "请求:" + getUrl() + " 返回时间：" + getNowTimeStr());
                    if (requestBodyType != null) {
                        logBuilder.i("<<<", requestBodyType.name() + "参数:\n" + formatParameterStr());
                    }
                    logBuilder.i("<<<", "返回内容:");

                    List<LockLog.LogBody> logBodyList = formatJson(result);
                    if (logBodyList == null) {
                        logBuilder.i("<<<", result);
                    } else {
                        logBuilder.add(logBodyList);
                    }
                    logBuilder.i("<<<", "=====================================");
                    logBuilder.build();
                }

                if (callbackInMainLooper) {
                    Looper mainLooper = Looper.getMainLooper();
                    handler = new Handler(mainLooper);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callCallbacks(resultBody, null);
                        }
                    });
                } else {
                    if (handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callCallbacks(resultBody, null);
                            }
                        });
                    } else {
                        callCallbacks(resultBody, null);
                    }
                }
            } else {
                byte[] buf = new byte[2048];
                int len = 0;
                long sum = 0;
                ResponseBody body = r.body();
                long total = body.contentLength();
                try (InputStream is = body.byteStream();
                     FileOutputStream fos = new FileOutputStream(downloadFile)) {
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        callDownloadingCallback(sum * 1.0f / total, sum, total);
                    }
                    fos.flush();
                }
            }
        } catch (Exception e) {
            onFail(e);
        }
    }

    private void callCallbacks(ResponseBody result, Exception e) {
        if (BaseOkHttpX.responseInterceptListener != null &&
                BaseOkHttpX.responseInterceptListener.onIntercept(BaseHttpRequest.this, result, e)) {
            return;
        }
        for (BaseResponseListener callback : callbacks) {
            callback.response(BaseHttpRequest.this, result, e);
        }
    }

    private void onFail(Exception e) {
        if (isShowLogs()) {
            LockLog.Builder logBuilder = LockLog.Builder.create()
                    .i("<<<", "-------------------------------------")
                    .i("<<<", "失败" + requestType.name() + "请求:" + getUrl() + " 失败时间：" + getNowTimeStr());

            if (requestBodyType != null) {
                logBuilder.i("<<<", requestBodyType.name() + "参数:\n" + formatParameterStr());
            }
            logBuilder.i("<<<", "错误信息:")
                    .e("<<<", getExceptionInfo(e))
                    .i("<<<", "=====================================")
                    .build();
        }
        deleteRequestInfo(requestInfo);
        if (BaseOkHttpX.reserveServiceUrls != null && BaseOkHttpX.reserveServiceUrls.length > 0) {
            String nextServiceUrl = findNextServiceUrl(BaseOkHttpX.reserveServiceUrls, BaseOkHttpX.serviceUrl);
            if (!isNull(nextServiceUrl)) {
                BaseOkHttpX.serviceUrl = nextServiceUrl;
                LockLog.logI("<<<", "尝试切换容灾服务器地址：" + nextServiceUrl + " 重新请求...");
                go();
                return;
            }
        }
        if (callbackInMainLooper) {
            Looper mainLooper = Looper.getMainLooper();
            handler = new Handler(mainLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callCallbacks(null, e);
                }
            });
        } else {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callCallbacks(null, e);
                    }
                });
            } else {
                callCallbacks(null, e);
            }
        }
    }

    private OkHttpClient createClient() {
        InputStream certificates = null;
        if (!isNull(BaseOkHttpX.forceValidationOfSSLCertificatesFilePath)) {
            try {
                certificates = new FileInputStream(new File(BaseOkHttpX.forceValidationOfSSLCertificatesFilePath));
            } catch (Exception e) {
                if (BaseOkHttpX.debugMode) e.printStackTrace();
            }
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(getTimeoutDuration(), TimeUnit.SECONDS)
                .writeTimeout(getTimeoutDuration(), TimeUnit.SECONDS)
                .readTimeout(getTimeoutDuration(), TimeUnit.SECONDS)
                .cache(getCacheSettings());
        if (certificates != null) {
            Object[] getSSLSocketFactoryResult = getSSLSocketFactory(certificates);
            if (getSSLSocketFactoryResult != null && getSSLSocketFactoryResult.length == 2) {
                SSLSocketFactory factory = (SSLSocketFactory) getSSLSocketFactoryResult[0];
                TrustManager[] trustManagers = (TrustManager[]) getSSLSocketFactoryResult[1];
                if (trustManagers != null && trustManagers.length > 0 && trustManagers[0] instanceof X509TrustManager) {
                    builder.sslSocketFactory(factory, (X509TrustManager) trustManagers[0]);
                } else {
                    builder.sslSocketFactory(factory);
                }
            }
        }
        if (proxy != null) {
            builder.proxy(proxy);
        }
        if (BaseOkHttpX.httpRequestDetailsLogs) {
            builder.eventListenerFactory(HttpEventListener.FACTORY);
        }
        if (BaseOkHttpX.keepCookies) {
            builder.cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    BaseOkHttpX.cookieStore.put(url, cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = BaseOkHttpX.cookieStore.get(url);
                    return cookies != null ? cookies : new ArrayList<>();
                }
            });
        }
        return builder.build();
    }

    private Request createRequest() {
        Request.Builder builder = new Request.Builder();
        RequestBody originRequestBody = createOriginRequestBody();
        RequestBody requestBody = originRequestBody == null ? null : new RequestBodyImpl(originRequestBody) {
            @Override
            public void onUploading(long current, long total, boolean done) {
                callUploadingCallback(current, total, done);
            }
        };
        switch (requestType) {
            case POST:
                if (requestBody != null) builder.post(requestBody);
                break;
            case PUT:
                if (requestBody != null) builder.put(requestBody);
                break;
            case PATCH:
                if (requestBody != null) builder.patch(requestBody);
                break;
            case DELETE:
                if (requestBody == null) {
                    builder.delete();
                } else {
                    builder.delete(requestBody);
                }
                break;
        }
        builder.url(getUrl());
        for (Map.Entry<String, Object> entry : getHeaderParameter().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue() + "");
        }
        if (!isNull(cookieStr)) {
            builder.addHeader("Cookie", cookieStr);
        }
        return builder.build();
    }

    private void callUploadingCallback(long current, long total, boolean done) {
        if (getUploadListener() == null) return;
        if (callbackInMainLooper) {
            Looper mainLooper = Looper.getMainLooper();
            handler = new Handler(mainLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    getUploadListener().onUpload(BaseHttpRequest.this, (float) current / total, current, total, done);
                }
            });
        } else {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        getUploadListener().onUpload(BaseHttpRequest.this, (float) current / total, current, total, done);
                    }
                });
            } else {
                getUploadListener().onUpload(BaseHttpRequest.this, (float) current / total, current, total, done);
            }
        }
    }

    private void callDownloadingCallback(float progress, long sum, long total) {
        if (getDownloadListener() == null) return;
        LockLog.logI("<<<", "下载：" + getUrl() + " 进度：" + progress + " 已下载：" + sum + " 总共：" + total);
        if (callbackInMainLooper) {
            Looper mainLooper = Looper.getMainLooper();
            handler = new Handler(mainLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    getDownloadListener().onDownload(BaseHttpRequest.this, downloadFile, progress, sum, total, progress >= 1f);
                }
            });
        } else {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        getDownloadListener().onDownload(BaseHttpRequest.this, downloadFile, progress, sum, total, progress >= 1f);
                    }
                });
            } else {
                getDownloadListener().onDownload(BaseHttpRequest.this, downloadFile, progress, sum, total, progress >= 1f);
            }
        }
    }

    private @Nullable RequestBody createOriginRequestBody() {
        if (requestBodyType != null) {
            if (BaseOkHttpX.parameterInterceptListener != null) {
                switch (requestBodyType) {
                    case STRING:
                        String parameterStr = String.valueOf(BaseOkHttpX.parameterInterceptListener.onIntercept(BaseHttpRequest.this, getUrl(), stringRequestParameter));
                        return RequestBody.create(isNull(parameterStr) ? "" : parameterStr, MediaType.parse(getRequestMimeType()));
                    case FILE:
                        return ((Parameter) BaseOkHttpX.parameterInterceptListener.onIntercept(BaseHttpRequest.this, getUrl(), getRequestParameter())).toFileParameter();
                    case FORM:
                        return ((Parameter) BaseOkHttpX.parameterInterceptListener.onIntercept(BaseHttpRequest.this, getUrl(), getRequestParameter())).toFormParameter();
                    case JSON:
                        return RequestBody.create(
                                ((Parameter) BaseOkHttpX.parameterInterceptListener.onIntercept(BaseHttpRequest.this, getUrl(), getRequestParameter()))
                                        .toParameterJsonMap().toString(), MediaType.parse(getRequestMimeType()));
                }
            } else {
                switch (requestBodyType) {
                    case STRING:
                        return RequestBody.create(isNull(stringRequestParameter) ? "" : stringRequestParameter, MediaType.parse(getRequestMimeType()));
                    case FILE:
                        return getRequestParameter().toFileParameter();
                    case FORM:
                        return getRequestParameter().toFormParameter();
                    case JSON:
                        return RequestBody.create(getRequestParameter().toParameterJsonMap().toString(), MediaType.parse(getRequestMimeType()));
                }
            }
        }
        return null;
    }

    private String getRealRequestUrl(String url) {
        String serviceUrl = BaseOkHttpX.serviceUrl;
        if (serviceUrl.endsWith("/") && url.startsWith("/")) {
            return serviceUrl + url.substring(1);
        }
        if (!serviceUrl.endsWith("/") && !url.startsWith("/")) {
            return serviceUrl + "/" + url;
        }
        return serviceUrl + url;
    }

    public String getUrl() {
        if (requestType == REQUEST_TYPE.GET) {
            Object parameter = getRequestParameter();
            String result;
            if (!url.startsWith("http")) {
                result = getRealRequestUrl(url);
            } else {
                result = url;
            }
            if (BaseOkHttpX.parameterInterceptListener != null) {
                parameter = BaseOkHttpX.parameterInterceptListener.onIntercept(BaseHttpRequest.this, result, parameter);
            }
            String parameterStr = (parameter instanceof Parameter) ? "?" + ((Parameter) parameter).toUrlParameter() : String.valueOf(parameter);
            return result + parameterStr;
        } else {
            if (!url.startsWith("http")) {
                return getRealRequestUrl(url);
            } else {
                return url;
            }
        }
    }

    public BaseHttpRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public BaseHttpRequest setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public BaseHttpRequest setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        return this;
    }

    public long getTimeoutDuration() {
        return timeoutDuration <= 0 ? (BaseOkHttpX.globalTimeOutDuration <= 0 ? 10 : BaseOkHttpX.globalTimeOutDuration) : timeoutDuration;
    }

    public BaseHttpRequest setTimeoutDuration(long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    public boolean isCallbackInMainLooper() {
        return callbackInMainLooper;
    }

    public BaseHttpRequest setCallbackInMainLooper(boolean callbackInMainLooper) {
        this.callbackInMainLooper = callbackInMainLooper;
        return this;
    }

    public Call getHttpCall() {
        return httpCall;
    }

    public REQUEST_TYPE getRequestType() {
        return requestType;
    }

    public BaseHttpRequest setRequestType(REQUEST_TYPE requestType) {
        this.requestType = requestType;
        return this;
    }

    protected void log(String data) {
        Log.i(">>>", data);
    }

    public String getRequestMimeType() {
        return requestMimeType;
    }

    public BaseHttpRequest setRequestMimeType(String requestMimeType) {
        this.requestMimeType = requestMimeType;
        return this;
    }

    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s) || "(null)".equals(s)) {
            return true;
        }
        return false;
    }

    public Parameter getHeaderParameter() {
        Parameter headers = headerParameter == null ? headerParameter = new Parameter(BaseOkHttpX.globalHeader) : headerParameter;
        if (BaseOkHttpX.headerInterceptListener != null) {
            headers = BaseOkHttpX.headerInterceptListener.onIntercept(BaseHttpRequest.this, getUrl(), headers);
        }
        return headers;
    }

    public Parameter getRequestParameter() {
        return requestParameter == null ? requestParameter = new Parameter(BaseOkHttpX.globalParameter) : requestParameter;
    }

    public BaseHttpRequest addParameter(String key, Object value) {
        getRequestParameter().add(key, value);
        if (requestBodyType == null)
            requestBodyType = value instanceof File ? REQUEST_BODY_TYPE.FILE : REQUEST_BODY_TYPE.FORM;
        return this;
    }

    public BaseHttpRequest addParameter(String key, Object... value) {
        getRequestParameter().add(key, value);
        requestBodyType = REQUEST_BODY_TYPE.FORM;
        return this;
    }

    public BaseHttpRequest setParameter(String parameter) {
        this.stringRequestParameter = parameter;
        if (requestBodyType == null) requestBodyType = REQUEST_BODY_TYPE.STRING;
        return this;
    }

    public BaseHttpRequest setParameter(Parameter parameter) {
        this.requestParameter = parameter;
        if (requestBodyType == null) requestBodyType = REQUEST_BODY_TYPE.FORM;
        return this;
    }

    public BaseHttpRequest setParameter(JsonMap parameter) {
        this.requestParameter = new Parameter(parameter);
        if (requestBodyType == null) requestBodyType = REQUEST_BODY_TYPE.JSON;
        requestMimeType = "application/json; charset=utf-8";
        return this;
    }

    public BaseHttpRequest setParameter(JSONObject parameter) {
        this.requestParameter = new Parameter(new JsonMap(parameter == null ? "" : parameter.toString()));
        if (requestBodyType == null) requestBodyType = REQUEST_BODY_TYPE.JSON;
        requestMimeType = "application/json; charset=utf-8";
        return this;
    }

    public BaseHttpRequest addHeader(String key, Object value) {
        getHeaderParameter().add(key, value);
        return this;
    }

    public BaseHttpRequest setHeader(Parameter parameter) {
        this.headerParameter = parameter;
        return this;
    }

    public REQUEST_BODY_TYPE getRequestBodyType() {
        return requestBodyType;
    }

    public BaseHttpRequest setRequestBodyType(REQUEST_BODY_TYPE requestBodyType) {
        this.requestBodyType = requestBodyType;
        return this;
    }

    public boolean isCallAsync() {
        return callAsync;
    }

    public BaseHttpRequest setCallAsync(boolean callAsync) {
        this.callAsync = callAsync;
        return this;
    }

    public boolean isRequesting() {
        return requesting;
    }

    private Timer timeoutChecker;
    private RequestInfo requestInfo;

    public void setRequesting(boolean requesting) {
        this.requesting = requesting;
        if (requesting) {
            timeoutChecker = new Timer();
            timeoutChecker.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isRequesting()) {
                        onFail(new TimeOutException());
                    }
                }
            }, timeoutDuration * 1000);
        } else {
            if (timeoutChecker != null) {
                timeoutChecker.cancel();
            }
        }
    }

    private static Object[] getSSLSocketFactory(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null) {
                        certificate.close();
                    }
                } catch (IOException e) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return new Object[]{sslContext.getSocketFactory(), trustManagerFactory.getTrustManagers()};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BaseHttpRequest bindLifecycleOwner(Context context) {
        if (context instanceof LifecycleOwner) {
            return bindLifecycleOwner((LifecycleOwner) context);
        }
        return this;
    }

    public BaseHttpRequest bindLifecycleOwner(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    cancel();
                }
            }
        });
        return this;
    }

    public void cancel() {
        handler = null;
        if (callbacks != null) {
            callbacks.clear();
        }
        if (timeoutChecker != null) {
            timeoutChecker.cancel();
        }
        if (httpCall != null) {
            httpCall.cancel(); // 取消未完成的请求
            httpCall = null;
        }
    }

    public BaseHttpRequest registerCallback(BaseResponseListener callback) {
        callbacks.add(callback);
        return this;
    }

    public BaseHttpRequest setCallback(BaseResponseListener callback) {
        return registerCallback(callback);
    }

    public BaseHttpRequest addCallback(BaseResponseListener callback) {
        return registerCallback(callback);
    }

    public BaseHttpRequest unregisterCallback(BaseResponseListener callback) {
        callbacks.remove(callback);
        return this;
    }

    public BaseHttpRequest removeCallback(BaseResponseListener callback) {
        return unregisterCallback(callback);
    }

    public BaseHttpRequest removeAllCallbacks() {
        callbacks.clear();
        return this;
    }

    public List<BaseResponseListener> getCallbacks() {
        return callbacks;
    }

    public Cache getCacheSettings() {
        return cacheSettings != undefinedCache ? cacheSettings : BaseOkHttpX.requestCacheSettings;
    }

    public BaseHttpRequest setCacheSettings(Cache cacheSettings) {
        this.cacheSettings = cacheSettings;
        return this;
    }

    public BaseHttpRequest downloadToFile(File file, DownloadListener downloadListener) {
        this.downloadFile = file;
        this.downloadListener = downloadListener;
        File dir = downloadFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return this;
    }

    public UploadListener getUploadListener() {
        return uploadListener;
    }

    public BaseHttpRequest setUploadListener(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
        return this;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }

    public BaseHttpRequest setShowLogs(boolean showLogs) {
        this.showLogs = showLogs;
        return this;
    }

    public boolean isShowLogs() {
        return showLogs && BaseOkHttpX.debugMode;
    }

    public String getCookieStr() {
        return cookieStr;
    }

    public BaseHttpRequest setCookieStr(String cookieStr) {
        this.cookieStr = cookieStr;
        return this;
    }

    protected String getNowTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
    }

    protected String findNextServiceUrl(String[] reserveServiceUrls, String serviceUrl) {
        if (reserveServiceUrls == null || reserveServiceUrls.length == 0) {
            return null;
        }
        for (int i = 0; i < reserveServiceUrls.length; i++) {
            if (Objects.equals(serviceUrl, reserveServiceUrls[i])) {
                if (i == reserveServiceUrls.length - 1) {
                    return null;
                } else {
                    return reserveServiceUrls[i + 1];
                }
            }
        }
        return reserveServiceUrls[0];
    }

    public boolean isStreamRequest() {
        return streamRequest;
    }

    public BaseHttpRequest setStreamRequest(boolean streamRequest) {
        this.streamRequest = streamRequest;
        return this;
    }
}
