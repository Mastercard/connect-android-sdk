package com.finicity.connect.sdk;

import android.webkit.WebView;
import android.webkit.WebViewClient;

class ConnectWebViewClient extends WebViewClient {

    private EventHandler eventHandler;
    private String connectUrl;

    public ConnectWebViewClient(EventHandler eventHandler, String connectUrl) {
        this.eventHandler = eventHandler;
        this.connectUrl = connectUrl;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // Compare finished URL to Connect link. For some reason an extra '/' is getting added
        // before the query string by the time it gets here.
        if (url.equals(connectUrl) || url.equals(connectUrl.replace("?", "/?"))) {
            eventHandler.onLoaded();
        }
    }
}
