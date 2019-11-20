package edu.bu.metcs.tacomotive;

import com.loopj.android.http.*;

/**
 * Utility class for making HTTP requests
 * Source: https://loopj.com/android-async-http/
 */
public class HttpUtils {
    private static final String BASE_URL = "https://api.yelp.com/v3/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "bearer n5JJRNBlussSj3N3Ufjw9YuV7q-4xnjTBRK_Ehj5zaLDnXWGzjV8PhA0RaQ4PDqeOZFInVjigcx0M1KEtZsEQ1mhuscYIvcAKq2CTUhqOj_NmiN_uz8imbdowGXrW3Yx");
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}