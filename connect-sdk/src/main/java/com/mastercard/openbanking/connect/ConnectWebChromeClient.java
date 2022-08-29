package com.mastercard.openbanking.connect;

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

    private Connect mConnect;
    private RelativeLayout mPopupViewContainer;
    private RelativeLayout mPopupLayout;
    private ImageButton mPopupCloseImgButton;
    private Button mPopupCloseTextButton;
    public static Boolean runningUnitTest = false;

    public ConnectWebChromeClient(Connect connect,
                                  RelativeLayout popupViewContainer,
                                  RelativeLayout popupLayout,
                                  ImageButton popupCloseImgButton,
                                  Button popupCloseTextButton) {
        this.mConnect = connect;
        this.mPopupViewContainer = popupViewContainer;
        this.mPopupLayout = popupLayout;
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

        // Also update Connect with new popupView
        mConnect.updatePopupView(popupView);

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
        if(mConnect.mFilePathCallback != null) {
            mConnect.mFilePathCallback.onReceiveValue(null);
        }

        mConnect.mFilePathCallback = filePathCallback;

        Intent intent = fileChooserParams.createIntent();

        try {
            mConnect.startActivityForResult(intent, Connect.SELECT_FILE_RESULT_CODE);
        } catch(ActivityNotFoundException e) {
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

    private WebView createPopupView() {
        final WebView popupView = new WebView(mConnect);

        popupView.getSettings().setJavaScriptEnabled(true);
        popupView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);

                mPopupLayout.setVisibility(View.GONE);
                mPopupViewContainer.removeView(popupView);
            }
        });

        popupView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        // Create close on-click listener
        View.OnClickListener closePopupListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnect.closePopup();
            }
        };

        // Setup popup close buttons
        mPopupCloseImgButton.setOnClickListener(closePopupListener);
        mPopupCloseTextButton.setOnClickListener(closePopupListener);

        return popupView;
    }

}
