package com.mastercard.openbanking.connect;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

class ConnectWebViewClient extends WebViewClient {

    private Connect mConnect;
    private EventHandler eventHandler;
    private String connectUrl;

    public ConnectWebViewClient(Connect connect, EventHandler eventHandler, String connectUrl) {
        this.mConnect = connect;
        this.eventHandler = eventHandler;
        this.connectUrl = connectUrl;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if(request != null && request.getUrl() != null && request.getUrl().toString().contains("config.json")){
            eventHandler.onLoad();
            mConnect.startPingTimer();
        }
        return super.shouldInterceptRequest(view, request);
    }
}
