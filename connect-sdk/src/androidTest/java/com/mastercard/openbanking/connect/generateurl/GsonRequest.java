package com.mastercard.openbanking.connect.generateurl;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {

    private static final String TAG = "GenUrl-GSON";

    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Response.Listener<T> listener;
    private final Object dataIn;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(int method, String url, Object dataIn, Class<T> clazz,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.dataIn = dataIn;
        this.clazz = clazz;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() {
        // Log.i(TAG,"getHeaders called");
        Map<String, String>  headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Finicity-App-Key", ConnectConfig.FINICITY_APP_KEY);
        if (!ConnectConfig.authToken.isEmpty()) {
            headers.put("Finicity-App-Token", ConnectConfig.authToken);
        }
        return headers;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    public byte[] getBody() {
//        Log.i(TAG,"getBody called");
//        String jsonStr = gson.toJson(dataIn);
//        Log.i(TAG, "getBody called, json: " + jsonStr);
        return gson.toJson(dataIn).getBytes();
    }

    @Override
    public String getBodyContentType() {
        // Log.i(TAG,"getBodyContentType called");
        return "application/json; charset=utf-8";
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
//            if (response != null) {
//                Log.i(TAG, "response statusCode: " + response.statusCode);
//            }
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}
