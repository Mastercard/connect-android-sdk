package com.finicity.connect.sdk;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

class ConnectWebChromeClient extends WebChromeClient {

    private RelativeLayout mPopupViewContainer;
    private RelativeLayout mPopupLayout;
    private WebView mPopupView;
    private ImageButton mPopupCloseImgButton;
    private Button mPopupCloseTextButton;

    public ConnectWebChromeClient(RelativeLayout popupViewContainer, RelativeLayout popupLayout,
                                  WebView popupView, ImageButton popupCloseImgButton,
                                  Button popupCloseTextButton) {
        this.mPopupViewContainer = popupViewContainer;
        this.mPopupLayout = popupLayout;
        this.mPopupView = popupView;
        this.mPopupCloseImgButton = popupCloseImgButton;
        this.mPopupCloseTextButton = popupCloseTextButton;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        // Create popup webview and add to popup view container
        WebView popupView = createPopupView();
        mPopupViewContainer.addView(popupView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        );

        // Set popup layout to visible
        mPopupLayout.setVisibility(View.VISIBLE);

        // Return popup view in resultMsg
        ((WebView.WebViewTransport) resultMsg.obj).setWebView(popupView);
        resultMsg.sendToTarget();

        return true;
    }

    @Override
    public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        if(Connect.CONNECT_INSTANCE.mFilePathCallback != null) {
            Connect.CONNECT_INSTANCE.mFilePathCallback.onReceiveValue(null);
        }

        Connect.CONNECT_INSTANCE.mFilePathCallback = filePathCallback;

        Intent intent = fileChooserParams.createIntent();

        try {
            Connect.CONNECT_INSTANCE.startActivityForResult(intent, Connect.SELECT_FILE_RESULT_CODE);
        } catch(ActivityNotFoundException e) {
            Connect.CONNECT_INSTANCE.mFilePathCallback = null;

            Toast.makeText(Connect.CONNECT_INSTANCE,
                    Connect.CONNECT_INSTANCE.getString(R.string.file_access_error_msg),
                    Toast.LENGTH_LONG).show();

            return false;
        }

        return true;
    }

    private WebView createPopupView() {
        this.mPopupView = new WebView(Connect.CONNECT_INSTANCE);

        mPopupView.getSettings().setJavaScriptEnabled(true);
        mPopupView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);

                mPopupLayout.setVisibility(View.GONE);
                mPopupViewContainer.removeView(mPopupView);
            }
        });

        mPopupView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        // Create close on-click listener
        View.OnClickListener closePopupListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect.CONNECT_INSTANCE.closePopup();
            }
        };

        // Setup popup close buttons
        mPopupCloseImgButton.setOnClickListener(closePopupListener);
        mPopupCloseTextButton.setOnClickListener(closePopupListener);

        return mPopupView;
    }

}
