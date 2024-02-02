package com.mastercard.openbanking.connect;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

class ConnectWebChromeClient extends WebChromeClient {

    private Connect mConnect;
    public static Boolean runningUnitTest = false;
    private EventHandler eventHandler;
    ConnectWebViewClientHandler connectWebViewClientHandler;
    private boolean isWebViewLoaded = false;

    public ConnectWebChromeClient(Connect connect,
                                  EventHandler eventHandler,ConnectWebViewClientHandler connectWebViewClientHandler) {
        this.mConnect = connect;
        this.eventHandler = eventHandler;
        this.connectWebViewClientHandler = connectWebViewClientHandler;
    }

    @Override
    public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        if (mConnect.mFilePathCallback != null) {
            mConnect.mFilePathCallback.onReceiveValue(null);
        }

        mConnect.mFilePathCallback = filePathCallback;

        Intent intent = fileChooserParams.createIntent();

        try {
            mConnect.startActivityForResult(intent, Connect.SELECT_FILE_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            mConnect.mFilePathCallback = null;

            if (!runningUnitTest) {
                Toast.makeText(mConnect,
                        mConnect.getString(R.string.file_access_error_msg),
                        Toast.LENGTH_LONG).show();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress == 100 && !isWebViewLoaded) {
            eventHandler.onLoad();
            mConnect.startPingTimer();
            connectWebViewClientHandler.handleOnPageFinish();
            isWebViewLoaded = true;
        }

    }
}