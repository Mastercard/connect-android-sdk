package com.mastercard.openbanking.connect;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
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
}