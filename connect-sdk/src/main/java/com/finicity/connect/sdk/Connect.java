package com.finicity.connect.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class Connect extends Activity {
    private static final String SDK_VERSION = "1.0.4";

    private static final String ALREADY_RUNNING_ERROR_MSG = "There is already another Connect Activity running. " +
            "Only 1 is allowed at a time. Please allow the current activity to finish " +
            "before launching a new Connect activity or finish it via the " +
            "Connect.finishCurrentActivity() method.";

    // Static stuff
    private static final String CONNECT_URL_INTENT_KEY = "com.finicity.connect.sdk.CONNECT_URL_INTENT_KEY";

    private static EventHandler EVENT_HANDLER;
    private static Connect CONNECT_INSTANCE;

    @Deprecated
    public static void start(Context context, String connectUrl, EventListener eventListener) {
        // Create EventHandler and call other start method
        EventListenerWrapper wrapper = new EventListenerWrapper(eventListener);

        Connect.start(context, connectUrl, wrapper);
    }

    public static void start(Context context, String connectUrl, EventHandler eventHandler) {
        if(Connect.CONNECT_INSTANCE != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        Intent connectIntent = new Intent(context, Connect.class);
        connectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        connectIntent.putExtra(Connect.CONNECT_URL_INTENT_KEY, connectUrl);

        // Set EventListener
        Connect.EVENT_HANDLER = eventHandler;

        context.startActivity(connectIntent);
    }

    // Layout stuff
    private RelativeLayout mMainLayout;
    private WebView mMainWebView;

    private RelativeLayout mPopupLayout;
    private ImageButton mPopupCloseImgButton;
    private Button mPopupCloseTextButton;
    private RelativeLayout mPopupViewContainer;
    private WebView mPopupView;

    // Upload
    protected static final int SELECT_FILE_RESULT_CODE = 100;
    protected ValueCallback<Uri[]> mFilePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * If the application process has been killed and resumed, onCreate is called
         * but Connect.EVENT_HANDLER is now null. Therefore this activity should be finished
         * to prevent errors. The application utilizing this framework should then restart
         * Connect.
         */
        if(Connect.EVENT_HANDLER == null) {
            Connect.CONNECT_INSTANCE = null;
            this.finish();
            return;
        }

        // Prevent calls to start when Connect is already running
        if(Connect.CONNECT_INSTANCE != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        // Save reference to this activity as static singleton
        Connect.CONNECT_INSTANCE = this;

        // Disable title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_connect);

        // Main layout and view
        this.mMainLayout = findViewById(R.id.mainLayout);
        this.mMainWebView = findViewById(R.id.mainWebView);
        mMainWebView.getSettings().setSupportMultipleWindows(true);
        mMainWebView.getSettings().setJavaScriptEnabled(true);
        mMainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mMainWebView.getSettings().setAllowFileAccess(true);

        this.mPopupLayout = findViewById(R.id.popupLayout);
        this.mPopupCloseImgButton = findViewById(R.id.popupCloseImgButton);
        this.mPopupCloseTextButton = findViewById(R.id.popupCloseTextButton);
        this.mPopupViewContainer = findViewById(R.id.popupViewContainer);

        mMainWebView.setWebChromeClient(new ConnectWebChromeClient(this,
                mPopupViewContainer, mPopupLayout, mPopupCloseImgButton,
                mPopupCloseTextButton));

        mMainWebView.setWebViewClient(new ConnectWebViewClient(Connect.EVENT_HANDLER, getIntent().getStringExtra(CONNECT_URL_INTENT_KEY)));

        // JS Interface and event listener for main WebView
        ConnectJsInterface jsInterface = new ConnectJsInterface(this, Connect.EVENT_HANDLER);
        mMainWebView.addJavascriptInterface(jsInterface, "Android");

        // Load configured URL
        mMainWebView.loadUrl(getIntent().getStringExtra(CONNECT_URL_INTENT_KEY));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == SELECT_FILE_RESULT_CODE) {
            if(resultCode != RESULT_CANCELED) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                    mFilePathCallback = null;
                }
            } else {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Connect.CONNECT_INSTANCE = null;
        Connect.EVENT_HANDLER = null;
        this.mPopupView = null;
    }

    // static method to finish the current activity, if there is one
    public static void finishCurrentActivity() {
        if(Connect.CONNECT_INSTANCE != null) {
            Connect.CONNECT_INSTANCE.finish();
        } else {
            throw new RuntimeException("There is no Connect Activity currently running");
        }
    }

    // Back Button functionality
    @Override
    public void onBackPressed() {
        if(mPopupLayout.getVisibility() == View.VISIBLE) {
            if(mPopupView.canGoBack()) {
                mPopupView.goBack();
            } else {
                DialogInterface.OnClickListener listener = getDialogClickListener();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.exit_confirmation_title))
                        .setMessage(getString(R.string.exit_confirmation_msg))
                        .setPositiveButton(getString(R.string.exit_confirmation_yes), listener)
                        .setNegativeButton(getString(R.string.exit_confirmation_no), listener).show();
            }
        } else {
            if(mMainWebView.canGoBack()) {
                mMainWebView.goBack();
            } else {
                try {
                    // Send cancel event and finish
                    Connect.EVENT_HANDLER.onCancel();

                    finish();
                } catch(Exception e) {
                    finish();
                }
            }
        }
    }

    private DialogInterface.OnClickListener getDialogClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    closePopup();
                }
            }
        };
    }

    protected void updatePopupView(WebView newPopupView) {
        this.mPopupView = newPopupView;

        // Fix user agent
        String defaultUserAgent = newPopupView.getSettings().getUserAgentString();
        String finUserAgent = defaultUserAgent.replace("; wv", "") + " Finicity-Connect-Mobile-SDK/" + SDK_VERSION;

        this.mPopupView.getSettings().setUserAgentString(finUserAgent);

    }

    protected void closePopup() {
        // Kill webview
        mPopupView.loadUrl("javascript:window.close();");
        mPopupView.destroy();
        mPopupView = null;

        // Hide popupLayout
        mPopupLayout.setVisibility(View.GONE);
        mPopupViewContainer.removeView(mPopupView);
    }
}
