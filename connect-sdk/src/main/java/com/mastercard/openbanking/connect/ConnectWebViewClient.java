package com.mastercard.openbanking.connect;

import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ConnectWebViewClient extends WebViewClient {

    private ConnectWebViewClientHandler connectWenViewClientHandler;


    public ConnectWebViewClient(ConnectWebViewClientHandler connectWenViewClientHandler) {
        this.connectWenViewClientHandler = connectWenViewClientHandler;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        connectWenViewClientHandler.handleOnPageFinish();
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        connectWenViewClientHandler.handleBadURLError();
    }
}
