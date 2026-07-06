package com.mastercard.openbanking.connect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


import org.json.JSONObject;


import java.util.Timer;
import java.util.TimerTask;
import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connect extends Activity implements ConnectWebViewClientHandler {
    private static final String SDK_VERSION = "3.1.0";

    private static final String ALREADY_RUNNING_ERROR_MSG = "There is already another Connect Activity running. " +
            "Only 1 is allowed at a time. Please allow the current activity to finish " +
            "before launching a new Connect activity or finish it via the " +
            "Connect.finishCurrentActivity() method.";

    // Static stuff
    private static final String CONNECT_URL_INTENT_KEY = "com.mastercard.openbanking.connect.CONNECT_URL_INTENT_KEY";
    private static final String CONNECT_REDIRECT_LINK_URL_INTENT_KEY = "com.mastercard.openbanking.connect.CONNECT_REDIRECT_LINK_URL_INTENT_KEY";

    private static EventHandler EVENT_HANDLER;
    private static WeakReference<Connect> CONNECT_INSTANCE_REF;
    private static WeakReference<ConnectJsInterface> jsInterfaceRef;
    public static boolean runningUnitTest = false;
    private static final String REDIRECT_URL_REGEX = "[a-z]://";
    private static final String INVALID_CHARACTERS_REGEX = "[!@#$%^&*]";

    private static String connectUrl;

    public static void start(Context context, String connectUrl, EventHandler eventHandler) {
        if (CONNECT_INSTANCE_REF != null && CONNECT_INSTANCE_REF.get() != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }
        Connect.connectUrl = connectUrl;

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
        if (CONNECT_INSTANCE_REF != null && CONNECT_INSTANCE_REF.get() != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        Connect.connectUrl = connectUrl;

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
    // Keep a reference to the WebChromeClient so we can notify it when external tabs are closed
    private ConnectWebChromeClient mWebChromeClient;



    // Upload
    protected static final int SELECT_FILE_RESULT_CODE = 100;
    protected ValueCallback<Uri[]> mFilePathCallback;
    private static final String DEFAULT_REDIRECT_URL = "connect://maob/redirect";

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
            if (CONNECT_INSTANCE_REF != null) CONNECT_INSTANCE_REF.clear();
            this.finish();
            return;
        }

        if (runningUnitTest) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        // Prevent calls to start when Connect is already running
        if (CONNECT_INSTANCE_REF != null && CONNECT_INSTANCE_REF.get() != null) {
            throw new RuntimeException(ALREADY_RUNNING_ERROR_MSG);
        }

        // Save reference to this activity as static singleton (weak to avoid leaks)
        CONNECT_INSTANCE_REF = new WeakReference<>(this);

        // Previously registered lifecycle listener removed; CustomTabsActivityManager notifies Connect directly.

        // Disable title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_connect);

        // Main layout and view
        this.mMainWebView = findViewById(R.id.mainWebView);
        mMainWebView.getSettings().setSupportMultipleWindows(true);
        mMainWebView.getSettings().setJavaScriptEnabled(true); //NOSONAR
        mMainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mMainWebView.getSettings().setAllowFileAccess(true); //NOSONAR
        mMainWebView.getSettings().setDomStorageEnabled(true);
        // create and keep a reference to the WebChromeClient
        mWebChromeClient = new ConnectWebChromeClient(this, Connect.EVENT_HANDLER, this);
        mMainWebView.setWebChromeClient(mWebChromeClient);


        // JS Interface and event listener for main WebView
        ConnectJsInterface js = new ConnectJsInterface(this, Connect.EVENT_HANDLER);
        mMainWebView.addJavascriptInterface(js, "maOBAndroidConnect");
        // Provide WebView and initial connect URL to the JS interface
        js.setWebView(mMainWebView);
        String initialUrl = getIntent().getStringExtra(CONNECT_URL_INTENT_KEY);
        js.setConnectUrl(initialUrl);
        // Keep a weak reference so other static callers can access it safely
        jsInterfaceRef = new WeakReference<>(js);
        // Inform WebChromeClient about JS interface so it can send messages to the page
        if (mWebChromeClient != null) {
            mWebChromeClient.setConnectJsInterface(js);
        }

        // mMainWebView.setWebContentsDebuggingEnabled(true); // Enable Chrome Dev Tools

        // Load configured URL (guard against null intent extra)

        if (initialUrl != null && !initialUrl.isEmpty()) {
            mMainWebView.loadUrl(initialUrl);
        } else {
            Log.w("Connect Android SDK", "No initial connect URL provided to load");
        }


        String redirectUrl = getIntent().getStringExtra(CONNECT_REDIRECT_LINK_URL_INTENT_KEY);

        if(redirectUrl != null && !redirectUrl.isEmpty() && !isValidUrl(redirectUrl)){
            Log.w("Connect Android SDK", "RedirectUrl is invalid please verify URL");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLinkIntent(intent);
    }

    /**
     * Handle incoming deep-link intents (scheme "connect://...").
     * This should be called from onCreate (initial intent) and onNewIntent (singleTask).
     */
    private void handleDeepLinkIntent(Intent intent) {
        if (intent == null) return;
        ConnectJsInterface js = jsInterfaceRef != null ? jsInterfaceRef.get() : null;

        if (js != null && js.isTrackPopupBlockedEventActive() && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String deepLink = data.toString();
                Log.i("Connect Android SDK", "Received deep link: " + deepLink);

                // Sanitize single quotes to avoid breaking the JS string literal
                String safeLink = deepLink.replace("'", "\\'");

                String javascript = String.format(
                        "window.postMessage({ type: 'window', closed: true, closed_by: '%s', action: 'closed', url: '%s' }, '%s')",
                        ConnectOauthCloseType.PARTNER_REDIRECTION.getValue(), safeLink, connectUrl
                );


                if (mMainWebView != null) {
                    mMainWebView.evaluateJavascript(javascript, null);
                }
            }
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
        if (CONNECT_INSTANCE_REF != null) CONNECT_INSTANCE_REF.clear();
        Connect.EVENT_HANDLER = null;
        ConnectJsInterface jsRef = jsInterfaceRef != null ? jsInterfaceRef.get() : null;
        if (jsRef != null) {
            jsRef.unbindCustomTabsService();
        }
        jsInterfaceRef = null;
    }


    /**
     * Notify the WebChromeClient that an OAuth flow was opened in an external FI app.
     * This allows the WebChromeClient to track child webview state and oauth URL.
     * @param url the OAuth URL that was opened in the external app
     */
    public void notifyOAuthOpenedInFiApp(String url) {
        if (mWebChromeClient != null) {
            mWebChromeClient.setOAuthURL(url);
            mWebChromeClient.setChildWebViewLoaded(true);
        }
    }

    /**
     * Post an OAuth-closed message to the host WebView via the JS interface.
     * This is used by external managers (e.g. CustomTabsActivityManager) to notify
     * the page that an OAuth window was closed by the user.
     * @param closeType the reason the OAuth window was closed
     */
    public void postWindowOauthCloseMessage(ConnectOauthCloseType closeType) {
        ConnectJsInterface js = jsInterfaceRef != null ? jsInterfaceRef.get() : null;
        if (js != null) {
            js.postWindowOauthCloseMessage(closeType);
        }
    }

    public void postWindowBlockedMessage() {
        ConnectJsInterface js = jsInterfaceRef != null ? jsInterfaceRef.get() : null;
        if (js != null) {
            js.postWindowBlockedMessage();
        }
    }

    // static method to finish the current activity, if there is one
    public static void finishCurrentActivity() {
        Connect current = CONNECT_INSTANCE_REF != null ? CONNECT_INSTANCE_REF.get() : null;
        if (current != null) {
            ConnectJsInterface js = jsInterfaceRef != null ? jsInterfaceRef.get() : null;
            if (js != null) {
                js.closeCustomTab();
            }
            current.finish();
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
        return (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                try {
                    // Send cancel event and finish
                    String message = "{ \"code\": \"100\", \"reason\": \"exit\" }";
                    JSONObject cancelEventData = new JSONObject(message);
                    Connect.EVENT_HANDLER.onCancel(cancelEventData);
                    finish();
                } catch (Exception e) {
                    finish();
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
                Connect current = CONNECT_INSTANCE_REF != null ? CONNECT_INSTANCE_REF.get() : null;
                if (current != null) {
                    current.runOnUiThread(() -> pingConnect());
                }
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
            javascript = "window.postMessage({ type: 'ping', sdkVersion: '" + SDK_VERSION + "', platform: 'Android',redirectUrl: '" + DEFAULT_REDIRECT_URL + "' }, '*')";
        }
        if (mMainWebView != null) {
            mMainWebView.evaluateJavascript(javascript, null);
        }


    }

    @Override
    public void handleOnPageFinish() {
        // handleOnPageFinish called
    }

    // CustomTabsLifecycleListener removed: lifecycle events are handled directly in CustomTabsActivityManager

    protected boolean isValidUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isEmpty() || redirectUrl.contains(" ")) {
            return false;
        }

        try {
            Uri uri = Uri.parse(redirectUrl);

            if (redirectUrl.startsWith("http") && (uri.getAuthority() == null || uri.getAuthority().isEmpty())) {
                return false;
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
