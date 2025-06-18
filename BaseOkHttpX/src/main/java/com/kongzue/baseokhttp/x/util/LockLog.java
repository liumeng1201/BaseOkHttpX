package com.kongzue.baseokhttp.x.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2025/6/18
 */
public class LockLog {

    private static final BlockingQueue<LogBody> logQueue = new LinkedBlockingQueue<>();
    public static LogListener logListener;

    static {
        Thread logThread = new Thread(() -> {
            try {
                while (true) {
                    LogBody log = logQueue.take(); // 阻塞等待日志
                    switch (log.getLevel()) {
                        case INFO:
                            Log.i(log.getTag(), log.getLog());
                            if (logListener != null) {
                                logListener.log(LogBody.LEVEL.INFO, log.getLog());
                            }
                            break;
                        case ERROR:
                            Log.e(log.getTag(), log.getLog());
                            if (logListener != null) {
                                logListener.log(LogBody.LEVEL.ERROR, log.getLog());
                            }
                            break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        logThread.setDaemon(true);
        logThread.start();
    }

    public static void logI(String tag, String s) {
        logQueue.offer(new LogBody(LogBody.LEVEL.INFO, tag, s));
    }

    public static void logE(String tag, Throwable e) {
        logQueue.offer(new LogBody(LogBody.LEVEL.ERROR, tag, getExceptionInfo(e)));
    }

    public static void logE(String tag, String e) {
        logQueue.offer(new LogBody(LogBody.LEVEL.ERROR, tag, e));
    }

    public static void log(List<LogBody> s) {
        if (s != null) {
            for (LogBody log : s) {
                if (log != null) {
                    logQueue.offer(log);
                }
            }
        }
    }

    public static class LogBody {

        public enum LEVEL {
            INFO,
            ERROR
        }

        private String tag, log;
        private final LEVEL level;

        public LogBody(LEVEL level, String tag, String log) {
            this.tag = tag;
            this.log = log;
            this.level = level;
        }

        public LogBody(String tag, String log) {
            this.tag = tag;
            this.log = log;
            this.level = LEVEL.INFO;
        }

        public String getTag() {
            return tag == null ? ">>>" : tag;
        }

        public LogBody setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public LEVEL getLevel() {
            return level;
        }

        public String getLog() {
            return log == null ? "" : log;
        }

        public LogBody setLog(String log) {
            this.log = log;
            return this;
        }
    }

    public static class Builder {

        private final List<LogBody> list;

        private Builder() {
            list = new CopyOnWriteArrayList<>();
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder i(String tag, String s) {
            list.add(new LogBody(tag, s));
            return this;
        }

        public Builder e(String tag, String s) {
            list.add(new LogBody(LogBody.LEVEL.ERROR, tag, s));
            return this;
        }

        public Builder add(List<LogBody> l) {
            if (l != null) list.addAll(l);
            return this;
        }

        public void build() {
            LockLog.log(list);
        }
    }

    public static String getExceptionInfo(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    public static List<LogBody> formatJson(String msg) {
        String message;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(4);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(4);
            } else {
                return null;
            }
        } catch (JSONException err) {
            return null;
        }

        String[] lines = message.split("\n");
        List<LogBody> logBodyList = new ArrayList<>();
        for (String line : lines) {
            logBodyList.add(new LogBody(LogBody.LEVEL.INFO, "<<<<<<", line));
        }
        return logBodyList;
    }

    public interface LogListener {
        void log(LogBody.LEVEL level, String logs);
    }
}