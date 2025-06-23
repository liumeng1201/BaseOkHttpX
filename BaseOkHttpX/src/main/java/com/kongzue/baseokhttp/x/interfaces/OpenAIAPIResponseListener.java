package com.kongzue.baseokhttp.x.interfaces;

import com.kongzue.baseokhttp.x.util.BaseHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenAIAPIResponseListener implements ResponseListener {

    StringBuilder resultBuilder = new StringBuilder();
    boolean isFinish;
    String optimizationContent;

    @Override
    public void response(BaseHttpRequest httpRequest, String line, Exception error) {
        if (error == null) {
            if (line.startsWith("data: ")) {
                String jsonData = line.substring(6).trim();
                if (jsonData.equals("[DONE]")) {
                    httpRequest.setRequesting(false);
                    isFinish = true;
                    onResponse(httpRequest, line, optimizationContent, error, isFinish);
                    onFinish(httpRequest, optimizationContent, error);
                    return;
                }
                String contentChunk = parseResponseJson(jsonData);
                if (contentChunk != null) {
                    resultBuilder.append(contentChunk);
                    optimizationContent = resultBuilder.toString().replaceAll("\n\n", "\n　　");
                    onResponse(httpRequest, line, optimizationContent, error, isFinish);
                }
            }
        } else {
            onResponse(httpRequest, line, optimizationContent, error, isFinish);
        }
    }

    private String parseResponseJson(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray choices = json.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject delta = choice.getJSONObject("delta");
                return delta.optString("content", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onResponse(BaseHttpRequest httpRequest, String subText, String fullResponseText, Exception error, boolean isFinish){};

    public void onFinish(BaseHttpRequest httpRequest, String fullResponseText, Exception error){

    }
}