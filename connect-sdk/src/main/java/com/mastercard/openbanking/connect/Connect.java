package com.mastercard.openbanking.connect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.browser.customtabs.CustomTabsIntent;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connect extends Activity implements ConnectWebViewClientHandler {
    private static final String SDK_VERSION = "3.0.3";

    private static final String ALREADY_RUNNING_ERROR_MSG = "There is already another Connect Activity running. " +
            "Only 1 is allowed at a time. Please allow the current activity to finish " +
            "before launching a new Connect activity or finish it via the " +
            "Connect.finishCurrentActivity() method.";

    // Static stuff
    private static final String CONNECT_URL_INTENT_KEY = "com.mastercard.openbanking.connect.CONNECT_URL_INTENT_KEY";
    private static final String CONNECT_REDIRECT_LINK_URL_INTENT_KEY = "com.mastercard.openbanking.connect.CONNECT_REDIRECT_LINK_URL_INTENT_KEY";

    private static EventHandler EVENT_HANDLER;
    private static Connect CONNECT_INSTANCE;
    private static ConnectJsInterface jsInterface;
    public static Boolean runningUnitTest = false;
    private final String REDIRECT_URL_REGEX = "[a-z]{1}://";
    private final String INVALID_CHARACTERS_REGEX = "[!@#$%^&*]";

    public static void start(Context context, String connectUrl, EventHandler eventHandler) {
        if (Connect.CONNECT_INSTANCE != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        Intent connectIntent = new Intent(context, Connect.class);
        if (runningUnitTest) {
            connectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        connectIntent.putExtra(Connect.CONNECT_URL_INTENT_KEY, connectUrl);

        // Set EventListener
        Connect.EVENT_HANDLER = eventHandler;

        context.startActivity(connectIntent);
    }


    public static void start(Context context, String connectUrl, String redirectUrl, EventHandler eventHandler) {
        if (Connect.CONNECT_INSTANCE != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        Intent connectIntent = new Intent(context, Connect.class);
        if (runningUnitTest) {
            connectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        connectIntent.putExtra(Connect.CONNECT_URL_INTENT_KEY, connectUrl);
        connectIntent.putExtra(Connect.CONNECT_REDIRECT_LINK_URL_INTENT_KEY, redirectUrl);

        // Set EventListener
        Connect.EVENT_HANDLER = eventHandler;
        context.startActivity(connectIntent);
    }
    private WebView mMainWebView;



    // Upload
    protected static final int SELECT_FILE_RESULT_CODE = 100;
    protected ValueCallback<Uri[]> mFilePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * If the application process has been killed and resumed, onCreate is called
         * but Connect.EVENT_HANDLER is now null. Therefore this activity should be finished
         * to prevent errors. The application utilizing this framework should then restart
         * Connect.
         */
        if (Connect.EVENT_HANDLER == null) {
            Connect.CONNECT_INSTANCE = null;
            this.finish();
            return;
        }

        if (runningUnitTest) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        // Prevent calls to start when Connect is already running
        if (Connect.CONNECT_INSTANCE != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        // Save reference to this activity as static singleton
        Connect.CONNECT_INSTANCE = this;

        // Disable title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_connect);

        // Main layout and view
        this.mMainWebView = findViewById(R.id.mainWebView);
        mMainWebView.getSettings().setSupportMultipleWindows(true);
        mMainWebView.getSettings().setJavaScriptEnabled(true);
        mMainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mMainWebView.getSettings().setAllowFileAccess(true);

        mMainWebView.setWebChromeClient(new ConnectWebChromeClient(this, Connect.EVENT_HANDLER,this));


        // JS Interface and event listener for main WebView
        jsInterface = new ConnectJsInterface(this, Connect.EVENT_HANDLER);
        mMainWebView.addJavascriptInterface(jsInterface, "maOBAndroidConnect");

        // mMainWebView.setWebContentsDebuggingEnabled(true); // Enable Chrome Dev Tools

        // Load configured URL
        mMainWebView.loadUrl(getIntent().getStringExtra(CONNECT_URL_INTENT_KEY));


        String redirectUrl = getIntent().getStringExtra(CONNECT_REDIRECT_LINK_URL_INTENT_KEY);

        if(redirectUrl != null && !redirectUrl.isEmpty() && !isValidUrl(redirectUrl)){
            Log.w("Connect Android SDK", "RedirectUrl is invalid please verify URL");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SELECT_FILE_RESULT_CODE) {
            if (resultCode != RESULT_CANCELED) {
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

        stopPingTimer();

        if (mMainWebView != null) {
            mMainWebView.destroy();
            mMainWebView = null;
        }
        Connect.CONNECT_INSTANCE = null;
        Connect.EVENT_HANDLER = null;
        Connect.jsInterface = null;
    }

    public void postWindowClosedMessage() {
        String javascript = "window.postMessage({ type: 'window', closed: true }, '*')";
        if (mMainWebView != null) {
            mMainWebView.evaluateJavascript(javascript, null);
        }
    }

    // static method to finish the current activity, if there is one
    public static void finishCurrentActivity() {
        if (Connect.CONNECT_INSTANCE != null) {
            if (jsInterface != null) {
                jsInterface.closeCustomTab();
            }
            Connect.CONNECT_INSTANCE.finish();
        } else {
            throw new RuntimeException("There is no Connect Activity currently running");
        }
    }

    // Back Button functionality
    @Override
    public void onBackPressed() {
        if (mMainWebView.canGoBack()) {
            mMainWebView.goBack();
        } else {
            DialogInterface.OnClickListener listener = getDialogClickListener();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.exit_confirmation_title))
                    .setMessage(getString(R.string.exit_confirmation_msg))
                    .setPositiveButton(getString(R.string.exit_confirmation_yes), listener)
                    .setNegativeButton(getString(R.string.exit_confirmation_no), listener).show();
        }
    }

    private DialogInterface.OnClickListener getDialogClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    try {
                        // Send cancel event and finish
                        String message = "{ \"code\": \"100\", \"reason\": \"exit\" }";
                        JSONObject jo = new JSONObject(message);
                        Connect.EVENT_HANDLER.onCancel(jo);
                        finish();
                    } catch (Exception e) {
                        finish();
                    }
                }
            }
        };
    }

    // Ping code to notify Connect of sdkVersion and platform type for analytics
    private Timer pingTimer;
    private TimerTask pingTimerTask;

    protected void startPingTimer() {
        stopPingTimer();

        pingTimer = new Timer();

        pingTimerTask = new TimerTask() {
            @Override
            public void run() {
                CONNECT_INSTANCE.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pingConnect();
                    }
                });
            }
        };
        pingTimer.schedule(pingTimerTask, 1000, 1000);
    }

    protected void stopPingTimer() {
        if (pingTimer != null) {
            pingTimer.cancel();
            pingTimer.purge();
        }
        if (pingTimerTask != null) {
            pingTimerTask.cancel();
        }
        pingTimer = null;
        pingTimerTask = null;
    }

    protected void pingConnect() {
        String redirectUrl = getIntent().getStringExtra(CONNECT_REDIRECT_LINK_URL_INTENT_KEY);
        String javascript;
        if (redirectUrl != null && !redirectUrl.isEmpty() && isValidUrl(redirectUrl) ) {
            javascript = "window.postMessage({ type: 'ping', sdkVersion: '" + SDK_VERSION + "', platform: 'Android', redirectUrl: '" + redirectUrl + "' }, '*')";
        } else {
            javascript = "window.postMessage({ type: 'ping', sdkVersion: '" + SDK_VERSION + "', platform: 'Android' }, '*')";
        }
        if (mMainWebView != null) {
            mMainWebView.evaluateJavascript(javascript, null);
        }


    }

    @Override
    public void handleOnPageFinish() {
        // handleOnPageFinish called
    }
    protected boolean isValidUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isEmpty() || redirectUrl.contains(" ")) {
            return false;
        }

        try {
            Uri uri = Uri.parse(redirectUrl);

            if (redirectUrl.startsWith("http")) {
                if (uri.getAuthority() == null || uri.getAuthority().isEmpty()) {
                    return false;
                }
            }

            if (containsInvalidCharacters(uri.getScheme()) || containsInvalidCharacters(uri.getAuthority())) {
                return false;
            }

            Pattern pattern = Pattern.compile(REDIRECT_URL_REGEX);
            Matcher matcher = pattern.matcher(redirectUrl);
            return matcher.find();
        } catch (Exception e) {
            return false;
        }
    }
    private boolean containsInvalidCharacters(String inputToTest) {
        Pattern invalidCharactersPattern = Pattern.compile(INVALID_CHARACTERS_REGEX);
        Matcher invalidCharactersMatcher = invalidCharactersPattern.matcher(inputToTest);
        return invalidCharactersMatcher.find();

    }
}
