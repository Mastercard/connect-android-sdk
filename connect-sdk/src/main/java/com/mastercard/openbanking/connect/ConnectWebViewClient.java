package com.mastercard.openbanking.connect;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class ConnectWebViewClient extends WebViewClient {

    private Connect mConnect;
    private EventHandler eventHandler;
    private String connectUrl;

    public ConnectWebViewClient(Connect connect, EventHandler eventHandler, String connectUrl) {
        this.mConnect = connect;
        this.eventHandler = eventHandler;
        this.connectUrl = connectUrl;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        // Compare finished URL to Connect link. For some reason an extra '/' is getting added
        // before the query string by the time it gets here.
        if (url.equals(connectUrl) || url.equals(connectUrl.replace("?", "/?"))) {
            eventHandler.onLoad();
            mConnect.startPingTimer();
        }
    }
}
