package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.util.JsonBean;
import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.x.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.x.exceptions.InstanceBeanException;
import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import java.lang.reflect.ParameterizedType;

public abstract class BeanResponseListener<T> extends ResponseListener {

    @Override
    public void response(BaseHttpRequest httpRequest, String response, Exception error) {
        T tInstance = null;
        Class<T> tClass;
        try {
            ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
            tClass = (Class<T>) pt.getActualTypeArguments()[0];
            tInstance = tClass.newInstance();
        } catch (Exception e) {
            //这种情况下没办法实例化泛型对象
            onResponse(httpRequest, null, new InstanceBeanException("请检查该 Bean 是否为 public 且其构造方法为 public"));
            return;
        }
        if (error == null) {
            JsonMap data = new JsonMap(response.toString());
            if (data.isEmpty()) {
                onResponse(httpRequest, tInstance, new DecodeJsonException(response.toString()));
            }
            tInstance = JsonBean.getBean(data, tClass);

            onResponse(httpRequest, tInstance, null);
        } else {
            onResponse(httpRequest, tInstance, error);
        }
    }

    public abstract void onResponse(BaseHttpRequest httpRequest, T data, Exception e);
}
