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
    public static boolean runningUnitTest = false;
    private EventHandler eventHandler;
    ConnectWebViewClientHandler connectWebViewClientHandler;
    protected boolean isWebViewLoaded = false;
    private boolean isChildWebViewLoaded = false;
    private WebView childWebView;
    private String oauthURL;
    private ConnectJsInterface connectJsInterface;

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

        if (newProgress >= 20 && !isWebViewLoaded) {
            mConnect.pingConnect();
            mConnect.startPingTimer();
            eventHandler.onLoad();
            connectWebViewClientHandler.handleOnPageFinish();
            isWebViewLoaded = true;
        }

    }

    /**
     * Called when the OAuth/child WebView is closed or dismissed.
     * Equivalent to safariViewControllerDidFinish in Swift.
     */
    public void onOAuthWebViewClosed() {
        if (isChildWebViewLoaded) {
            // Post window closed message to parent WebView
//            if (connectJsInterface != null) {
//                connectJsInterface.postWindowOauthCloseMessage(ConnectOauthCloseType.USER_CLOSED);
//            }

            // Clean up resources
            oauthURL = null;
            isChildWebViewLoaded = false;
            childWebView = null;
        }
    }

    /**
     * Called when the OAuth/child WebView completes its initial load.
     * Equivalent to safariViewController:didCompleteInitialLoad: in Swift.
     *
     * @param didLoadSuccessfully true if the page loaded successfully, false if it failed
     */
    public void onOAuthWebViewLoadComplete(boolean didLoadSuccessfully) {
        if (!didLoadSuccessfully) {
            // If load failed, post blocked message to parent WebView
            if (connectJsInterface != null) {
                connectJsInterface.postWindowBlockedMessage();
            }
        } else {
            // If load succeeded, post OAuth open message with secureContainer type
            if (connectJsInterface != null) {
                connectJsInterface.postWindowOauthOpenMessage(ConnectOauthOpenType.SECURE_CONTAINER);
            }
        }
    }

    /**
     * Set the ConnectJsInterface instance for communication with the parent WebView
     * @param jsInterface the ConnectJsInterface to use for posting messages
     */
    public void setConnectJsInterface(ConnectJsInterface jsInterface) {
        this.connectJsInterface = jsInterface;
    }

    /**
     * Track when a child/OAuth WebView has been loaded
     * @param loaded true if a child WebView is loaded
     */
    public void setChildWebViewLoaded(boolean loaded) {
        this.isChildWebViewLoaded = loaded;
    }

    /**
     * Get the current OAuth URL
     * @return the OAuth URL
     */
    public String getOAuthURL() {
        return oauthURL;
    }

    /**
     * Set the OAuth URL being opened
     * @param url the OAuth URL
     */
    public void setOAuthURL(String url) {
        this.oauthURL = url;
    }

    /**
     * Set the reference to the child/OAuth WebView
     * @param webView the child WebView
     */
    public void setChildWebView(WebView webView) {
        this.childWebView = webView;
    }

    /**
     * Get the reference to the child/OAuth WebView
     * @return the child WebView
     */
    public WebView getChildWebView() {
        return childWebView;
    }

}
