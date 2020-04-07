package com.finicity.connect.sdk;

import android.webkit.WebView;
import android.webkit.WebViewClient;

class ConnectWebViewClient extends WebViewClient {

    private EventListener eventListener;
    private String connectUrl;

    public ConnectWebViewClient(EventListener eventListener, String connectUrl) {
        this.eventListener = eventListener;
        this.connectUrl = connectUrl;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // Compare finished URL to Connect link. For some reason an extra '/' is getting added
        // before the query string by the time it gets here.
        if(url.equals(connectUrl.replace("?", "/?"))) {
            eventListener.onLoaded();
        }
    }
}
