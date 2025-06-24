package com.kongzue.baseokhttp.x.util;

/**
 * Created by myzcx on 2018/1/22.
 */

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.kongzue.baseokhttp.util.JsonMap;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Parameter extends TreeMap<String, Object> {

    public Parameter() {

    }

    public Parameter(Map<String, ?> otherMap) {
        if (otherMap != null) {
            for (Map.Entry<String, ?> entry : otherMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key != null && value != null) {
                    this.put(key, value);
                }
            }
        }
    }

    public Parameter(Map<String, ?>... maps) {
        if (maps != null) {
            for (Map<String, ?> map : maps) {
                if (map != null) {
                    for (Map.Entry<String, ?> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (key != null && value != null) {
                            this.put(key, value);
                        }
                    }
                }
            }
        }
    }

    public Parameter add(String key, Object value) {
        put(key, value);
        return this;
    }

    public String toParameterString() {
        String result = "";
        if (!entrySet().isEmpty()) {
            for (Entry<String, Object> entry : entrySet()) {
                if (entry.getValue() != null && entry.getValue().getClass().isArray()) {
                    int length = Array.getLength(entry.getValue());
                    for (int i = 0; i < length; i++) {
                        Object element = Array.get(entry.getValue(), i);
                        result = result + entry.getKey() + "=" + element + "&";
                    }
                } else {
                    result = result + entry.getKey() + "=" + entry.getValue() + "&";
                }
            }
            if (result.endsWith("&")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    public RequestBody toFormParameter() {
        RequestBody requestBody;

        FormBody.Builder builder = new FormBody.Builder();
        for (Entry<String, Object> entry : entrySet()) {
            if (entry.getValue() != null && entry.getValue().getClass().isArray()) {
                int length = Array.getLength(entry.getValue());
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(entry.getValue(), i);
                    builder.add(String.valueOf(entry.getKey()), String.valueOf(element));
                }
            } else {
                builder.add(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }

        requestBody = builder.build();
        return requestBody;
    }

    public RequestBody toFileParameter() {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Entry<String, Object> entry : entrySet()) {
            if (entry.getValue() instanceof File) {
                File file = (File) entry.getValue();
                multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(file, MediaType.parse(getMimeType(file))));
            } else {
                if (entry.getValue() != null && entry.getValue().getClass().isArray()) {
                    int length = Array.getLength(entry.getValue());
                    for (int i = 0; i < length; i++) {
                        Object element = Array.get(entry.getValue(), i);
                        multipartBuilder.addFormDataPart(String.valueOf(entry.getKey()), String.valueOf(element));
                    }
                } else {
                    multipartBuilder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return multipartBuilder.build();
    }

    public String toUrlParameter() {
        String result = "";
        try {
            if (!entrySet().isEmpty()) {
                for (Entry<String, Object> entry : entrySet()) {
                    if (entry.getValue() != null && entry.getValue().getClass().isArray()) {
                        int length = Array.getLength(entry.getValue());
                        for (int i = 0; i < length; i++) {
                            Object element = Array.get(entry.getValue(), i);
                            result = result + encodeUrl(entry.getKey()) + "=" + encodeUrl(String.valueOf(element)) + "&";
                        }
                    }else{
                        result = result + encodeUrl(entry.getKey()) + "=" + encodeUrl(String.valueOf(entry.getValue())) + "&";
                    }
                }
                if (result.endsWith("&")) {
                    result = result.substring(0, result.length() - 1);
                }
            }
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    private String encodeUrl(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    public JSONObject toParameterJson() {
        JSONObject result = new JSONObject();
        try {
            if (!entrySet().isEmpty()) {
                for (Entry<String, Object> entry : entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public JsonMap toParameterJsonMap() {
        JsonMap result = new JsonMap();
        try {
            if (!entrySet().isEmpty()) {
                for (Entry<String, Object> entry : entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        Object value = get(key);
        if (isNull(String.valueOf(value))) {
            return defaultValue;
        }
        return value == null ? "" : String.valueOf(value);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int emptyValue) {
        int result = emptyValue;
        try {
            result = Integer.parseInt(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean emptyValue) {
        boolean result = emptyValue;
        try {
            result = Boolean.parseBoolean(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long emptyValue) {
        long result = emptyValue;
        try {
            result = Long.parseLong(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    public short getShort(String key, short emptyValue) {
        short result = emptyValue;
        try {
            result = Short.parseShort(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double emptyValue) {
        double result = emptyValue;
        try {
            result = Double.parseDouble(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    public float getFloat(String key, float emptyValue) {
        float result = emptyValue;
        try {
            result = Float.parseFloat(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }

    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }

    public String getMimeType(File file) {
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (!isNull(type)) {
            return type;
        }
        return "file/*";
    }

    private static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    public String toString(BaseHttpRequest.REQUEST_BODY_TYPE type) {
        switch (type) {
            case JSON:
                return toParameterJsonMap().toString(4);
            default:
                return toString();
        }
    }

    @Override
    public String toString() {
        return toParameterString();
    }
}