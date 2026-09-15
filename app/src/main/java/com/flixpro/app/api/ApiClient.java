package com.flixpro.app.api;

import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {
    public static final String SERVER = "https://hotflix.vip/";
    public static final String API = SERVER + "api/v1/";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public interface Callback {
        void onSuccess(JSONObject root);
        void onError(Exception error);
    }

    public static void post(String endpoint, JSONObject payload, Callback callback) {
        post(endpoint, payload, null, callback);
    }

    public static void post(String endpoint, JSONObject payload, Integer page, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(endpoint.startsWith("http") ? endpoint : API + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "FlixPro/2.0 Android");

                String json = payload == null ? "{}" : payload.toString();
                String base64 = Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
                StringBuilder body = new StringBuilder("data=")
                        .append(URLEncoder.encode(base64, "UTF-8"));
                if (page != null) body.append("&page=").append(page);

                byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(out.length);
                try (OutputStream os = connection.getOutputStream()) { os.write(out); }

                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 400 ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder text = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) text.append(line);
                    }
                }
                if (code < 200 || code >= 400) throw new IllegalStateException("HTTP " + code + ": " + text);
                callback.onSuccess(new JSONObject(text.toString()));
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    public static JSONObject appObject(JSONObject root) {
        return root.optJSONObject("VIDEO_STREAMING_APP");
    }

    public static String normalizeImage(String url) {
        if (url == null || url.trim().isEmpty() || "null".equalsIgnoreCase(url)) return "";
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        if (url.startsWith("/")) return SERVER.substring(0, SERVER.length() - 1) + url;
        return SERVER + url;
    }
}
