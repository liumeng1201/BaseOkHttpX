<div align=center>    
    <img src="https://github.com/kongzue/BaseOkHttpX/blob/main/readme/baseokhttpx.png" width="130">    
    <center><h1>Kongzue BaseOkHttpX</h1></center> 
</div>

基于 OkHttp 的二次封装网络请求框架，更简洁易用，符合应用开发者使用习惯，能够自动处理异步线程问题，具备丰富的扩展性，全局拦截器以及实用的日志输出控制，让网络请求变得更加简单。

<div align=center>
  <a href="https://github.com/kongzue/BaseOkHttpX/">
    <img src="https://img.shields.io/badge/Kongzue%20BaseOkHttpX-Release-green.svg" alt="Kongzue BaseOkHttpX">
  </a> 
  <a href="https://jitpack.io/#kongzue/BaseOkHttpX">
    <img src="https://jitpack.io/v/kongzue/BaseOkHttpX.svg" alt="Jitpack.io">
  </a> 
  <a href="http://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
  </a> 
  <a href="http://www.kongzue.com">
    <img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
  </a>
</div>

## BaseOkHttpX 优势

- **全新升级：** BaseOkHttpX 是 [BaseOkHttpV3](https://github.com/kongzue/BaseOkHttpV3) 的升级重构版本，重做了因旧版代码不断迭代导致的冗余问题，代码结构更为清晰明了

- **简单易用：** BaseOkHttpX 的请求代码非常简单，只需几句即可直接完成请求操作，无论是创建 Get、Post、Put、Patch 还是 Delete 请求，还是上传下载，亦或者添加各种参数，都可以灵活应对。

- **线程安全：** 你也无需担心处理线程问题，请求会发生在异步线程，而请求结果的回调会自动返回主线程处理接下来的事务，您只需要专注 UI 逻辑即可，剩下的交给 BaseOkHttpX。

- **Json友好：** BaseOkHttpX 同样默认使用 [BaseJson](https://github.com/kongzue/BaseJson) 作为 json 请求和返回数据的处理实现，BaseJson 默认将 json 文本转换为 Map、List 对象，因此你可以轻松的直接在安卓的列表适配器中使用它们，且更易于持久化存储和读取。

- **全局处理：** BaseOkHttpX 支持全局的拦截器，无论是全局请求参数、全局请求头还是全局回调拦截都可以轻松实现。

## Demo

[![下载Demo](https://github.com/kongzue/BaseOkHttpX/blob/main/readme/demo_download.png)](https://github.com/kongzue/BaseOkHttpX/releases/download/1.2.0/app-release.apk)

## 如何引入项目

BaseOkHttpX 发布在 Jitpack，你可以在 [Jitpack](https://jitpack.io/#kongzue/BaseOkHttpX) 最新版本和仓库信息

最新版本： <a href="https://jitpack.io/#kongzue/BaseOkHttpX"><img src="https://jitpack.io/v/kongzue/BaseOkHttpX.svg" alt="Jitpack.io"></a> 

要引入到您的安卓项目，首先请在安卓项目根目录的 `settings.gradle` 文件里添加：

```gradle
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }    // ← 添加这行
  }
}
```

然后，在您项目 app 的 `build.gradle` 文件中的 `dependencies` 代码块中添加：

```gradle
dependencies {
    // ...其它引入，添加下边的代码：
    // BaseOkHttpX 
    implementation 'com.github.kongzue:BaseOkHttpX:1.2.0'
    // BaseJson
    implementation 'com.github.kongzue:BaseJson:1.2.8'
    // OkHttp
    implementation 'com.squareup.okhttp3:okhttp:4.9.1'
}
```

点击屏幕右上角的 `Sync Now` 同步 Gradle 即可引入 BaseOkHttpX。

另外网络请求需要在项目的 `AndroidManifest.xml` 文件中 `<manifest>` 标签内添加权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 快速上手

### 创建请求

简单创建一个 Get 请求：

```java
Get.create("/api/sentences")
        .setCallback(new JsonResponseListener() {
            @Override
            public void onResponse(BaseHttpRequest httpRequest, JsonMap main, Exception error) {
                binding.txtResult.setText(main.toString());
            }
        })
        .go();
```

同样的，`Post` 请求、`Put` 请求等其它类型的请求均可直接使用 `.create(...)` 方法创建。

还有一种简化方法，可以直接使用静态方法构建请求：

```java
// 创建一个 Get 请求
getRequest("/api/sentences")

// 创建一个 Post 请求
postRequest(MainActivity.this, "/api/login")
```

其中首位参数 `context` 是可选的，如果传入 BaseOkHttpX 也不会持有 context 以避免内存泄漏，如果 context 具备生命周期则会绑定一个监听，当 context 生命周期结束时自动取消正在发生的请求和未执行的回调，一般来说也可以忽略这个参数，区别只在于不传入 context 那么回调中的非 UI 的逻辑依然会被执行。

### 回调处理

请求结束处理回调时，除了上述可以通过 `setCallback(...)` 设置回调，也可以简写在 `go()` 方法中：

```java
.go(new JsonResponseListener() {
    @Override
    public void onResponse(BaseHttpRequest httpRequest, JsonMap main, Exception error) {
        binding.txtResult.setText(main.toString());
    }
});
```

BaseOkHttpX 采用统一回调，即请求成功与失败均会在 onResponse 中返回，您只需要关注 `error` 参数是否为空即可判断请求成功，其中返回的数据 `main` 永远不为空，这意味着如果需要简写，您完全可以忽略判断 `error`，只关注 `main` 参数即可，范例如下：

```java
.go(new JsonResponseListener() {
    @Override
    public void onResponse(BaseHttpRequest httpRequest, JsonMap main, Exception error) {
        if (!main.isEmpty()){
            // 处理正常请求接下来的业务...
        }else{
            // 弹出UI提示：请求失败，原因根据 error 类型判断
        }
    }
});
```

这样设计的目的在于避免 app 开发者反复判断空指针和覆写不必要的方法，可以专注于业务逻辑的开发，不用担心繁琐的事务处理。

### 添加参数

BaseOkHttpX 默认支持文本、Json、Form表单和上传文件的参数类型，要添加参数可以通过 `addParameter()` 方法进行：

```java
.addParameter("key1", "value1")
.addParameter("key2", "value2")
```

**Tips:** 如果添加的 value 为 `File` 对象，那么请求将会被当做文件上传进行。

也可以通过 `setParameter()` 指定一个 Parameter 对象：

```java
.setParameter(new Parameter()
        .add("key1", "value1")
        .add("key2", "value2")
)
```

还可以直接指定文本请求体：

```
.setParameter("string parameter...")
```

添加数组参数：

```java
.addParameter("ids[]", 1, 2, 3, 4, 5)
```

数组参数会以相同的 key 和不同的值进行请求。

或者 jsonMap 或者 JSONObject 对象：

```java
.setParameter(new JsonMap()
        .set("account", "username")
        .set("password", "123456")
)
```

**Tips:** 使用 json 参数时 mimeType 默认会指定为 `application/json; charset=utf-8`，要修改或自定义 mimeType 可以使用方法 `.setRequestMimeType()`。

#### 添加 Header 请求头

添加 Header 请求头可以通过以下方法进行添加：

```java
.addHeader("key1", "value1")
//或
.addHeader(new Parameter()
        .add("key1", "value1")
        .add("key2", "value2")
)
```

## 全局设置

BaseOkHttpX 拥有很多全局设置可以轻松帮助你完成诸如全局服务器地址、全局参数、全局 Header、全局返回逻辑处理、容灾服务器地址等设置和事务的处理，你可以直接使用静态变量来设置这些参数：

```java
// 指定服务器 URL，你的实际请求地址如果以非“http”开头则会自动在首位拼接服务器地址为完整地址后发起请求
BaseOkHttpX.serviceUrl = (String)

// 是否调试模式，开启此功能将会在 Logcat 打印请求相关的日志，注意在 app 编译为 release 发布版本时请关闭此开关
BaseOkHttpX.debugMode = (boolean)

// 请求超时时长（单位：秒），默认 10
BaseOkHttpX.globalTimeOutDuration = (int)

// 缓存请求: new Cache(path, cacheSize)，默认为 null
BaseOkHttpX.requestCacheSettings = (Cache)

// 容灾地址，当默认 serviceUrl 请求不通时会逐个使用容灾地址作为服务器地址发起请求
BaseOkHttpX.reserveServiceUrls = (String[])

// 全局请求拦截器，如果有设置，所有请求返回时都会优先走拦截器，当在拦截器中 return true 时请求回调会被拦截。
BaseOkHttpX.responseInterceptListener = (ResponseInterceptListener)

// 全局请求参数拦截器，可以在这里对原请求参数进行处理，例如通过加密算法对参数进行签名等
BaseOkHttpX.parameterInterceptListener = (ParameterInterceptListener)

// 全局请求头拦截器，同上
BaseOkHttpX.headerInterceptListener = (HeaderInterceptListener)

// 是否禁止重复请求，当打开此设置时，同一个 url 和参数的请求在上个请求未结束时发起则会被拦截
BaseOkHttpX.disallowSameRequest = (boolean)

// 全局请求参数
BaseOkHttpX.globalParameter = (Parameter)

// 全局请求头
BaseOkHttpX.globalHeader = (Parameter)

// 强制验证放置在 SSL 证书路径（当值不为空时生效）
BaseOkHttpX.forceValidationOfSSLCertificatesFilePath = (String)

// 是否保留 Cookies
BaseOkHttpX.keepCookies = (boolean)

// 已保留的所有 Cookies
BaseOkHttpX.cookieStore = (HashMap<HttpUrl, List<Cookie>>)

// 启用更详细的请求事件日志
BaseOkHttpX.httpRequestDetailsLogs = (boolean)
```

## 上传和下载

### 上传文件

在设置请求参数时传入 File 类型的 value 即代表上传文件。

```java
.addParameter("file1",new File(getCacheDir(),"cache1.jpg"))
.addParameter("file2",new File(getCacheDir(),"cache2.jpg"))
```

当请求是上传文件时，可以通过以下方法监听上传进度：

```java
.setUploadListener(new UploadListener() {
    @Override
    public void onUpload(BaseHttpRequest httpRequest, float progress, long current, long total, boolean done) {
        
    }
})
```

其中 `progress` 参数为进度，范围 `[0.0f~1.0f]`，`current`为已上传字节数，`total` 为总字节数，`done` 代表是否完成。

### 下载文件

要下载文件可以使用以下代码进行：

```java
getRequest(MainActivity.this, "https://dl.coolapk.com/down?pn=com.coolapk.market&id=NDU5OQ&h=46bb9d98&from=from-web")
        .downloadToFile(cacheFile, new DownloadListener() {
            @Override
            public void onDownload(BaseHttpRequest httpRequest, File downloadFile, float progress, long current, long total, boolean done) {
            }
        })
        .go();
```

相关参数与上传回调相似。

### 流式输出

使用 `.setStreamRequest(true)` 可直接开启流式输出。

可以在回调中动态接收服务器返回的结果：

```java
.go(new ResponseListener() {
    @Override
    public void response(BaseHttpRequest httpRequest, String response, Exception error) {
        // 其中 response 为每次服务器流式返回的文本，需要自行处理拼接之前返回的数据
    }
});
```

### OpenAI API 标准请求

如果你使用的是标注 OpenAI API 请求，例如 ChatGPT、DeepSeek、火山引擎、OpenRouter 等均支持使用 OpenAI API 标准请求的情况下，可使用 `OpenAIAPIResponseListener` 回调协助处理服务器流式输出的文本拼接。

范例如下：

```java
Post.create("https://api.deepseek.com/chat/completions")
        .setStreamRequest(true)
        .addHeader("Authorization", "Bearer " + deepSeekAPIKey)         // apiKeys 需要您自行通过服务商后台申请
        .addHeader("Content-Type", "application/json")
        .setParameter(new JsonMap()
                .set("model", "deepseek-chat")                          // 以 DeepSeek 举例，具体模型请依据服务商调整
                .set("messages", new JsonList()
                        .set(new JsonMap()
                                .set("role", "user")
                                .set("content", "你是什么模型？能为我提供什么帮助？")    // 用户提示词
                        )
                )
                .set("stream", true)
                .set("temperature", 0.7))                               // 热度
                .go(new OpenAIAPIResponseListener() {                   // 使用 OpenAIAPIResponseListener
                    @Override
                    public void onResponse(BaseHttpRequest httpRequest, String subText, String fullResponseText, Exception error, boolean isFinish) {
                        // 请注意此处 onResponse 方法会回调多次。
                        // 参数中，subText 是每次服务器返回的文本，fullResponseText 是完成拼接的文本，error 是错误信息（null代表请求正常继续），isFinish 为响应是否结束。
                        binding.txtResult.setText(fullResponseText);
                    }
                });
```

如果你想一次性直接等待服务器完整返回后处理结果，方案 1 是修改请求参数 json 中的 `stream = false`，方案 2 是复写 `OpenAIAPIResponseListener` 中的 `onFinish` 方法回调：

```java
OpenAIAPIResponseListener(){
    @Override
    public void onFinish(BaseHttpRequest httpRequest,String fullResponseText,Exception error){
        // 直接使用 fullResponseText 完整结果即可
    }
}
```

## 日志输出

BaseOkHttpX 的日志输出会在请求发起时和返回时进行打印，通过 `BaseOkHttpX.debugMode = true` 开启日志输出即可在 Logcat 中看到打印的请求日志：

完整的日志请求举例：
```
-------------------------------------
发出GET请求:https://api.apiopen.top/api/sentences?customKey=customValue&ids%5B%5D=1&ids%5B%5D=2&ids%5B%5D=3&ids%5B%5D=4&ids%5B%5D=5&t1=v1 请求时间：2025-06-18 22:45:45.527
FORM参数:
customKey=customValue&ids[]=1&ids[]=2&ids[]=3&ids[]=4&ids[]=5&t1=v1
=====================================
tagSocket(4) with statsTag=0xffffffff, statsUid=-1
-------------------------------------
成功GET请求:https://api.apiopen.top/api/sentences?customKey=customValue&ids%5B%5D=1&ids%5B%5D=2&ids%5B%5D=3&ids%5B%5D=4&ids%5B%5D=5&t1=v1 返回时间：2025-06-18 22:45:45.783
FORM参数:
customKey=customValue&ids[]=1&ids[]=2&ids[]=3&ids[]=4&ids[]=5&t1=v1
返回内容:
{
    "code": 200,
    "message": "成功!",
    "result": {
        "name": "柳花惊雪浦，麦雨涨溪田。",
        "from": "李贺《南园十三首》"
    }
}
=====================================
```

## 额外设置

Get、Post、Delete、Patch、Put 在创建请求后会返回实例化的 `BaseHttpRequest` 对象，BaseHttpRequest 还支持一些细节设置，例如：
```java
// 单独设置本次请求是否输出日志
.setShowLogs(boolean)

// 单独设置本次请求超时时间
.setTimeoutDuration(long) 

// 单独设置本次请求在当前线程执行
.setCallAsync(boolean)    
        
// 本次请求务必在主线程执行回调
.setCallbackInMainLooper(boolean)
        
// 修改本次请求的请求类型
.setRequestType(REQUEST_TYPE)

// 判断是否正在请求中
(boolean) = .isRequesting()
```

## ToDo

BaseOkHttpX 刚刚完成，还有部分不足，以下是一个待办清单，将会在后续版本中补足计划中的功能：

```
- WebSocket 支持
- 其他类型的请求结果
```

## 开源协议

BaseOkHttpX 遵循 Apache License 2.0 开源协议。

```
Copyright Kongzue BaseOkHttpX

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

