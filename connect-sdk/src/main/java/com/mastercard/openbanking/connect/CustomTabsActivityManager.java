package com.mastercard.openbanking.connect;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.lang.ref.WeakReference;

public class CustomTabsActivityManager extends Activity {

    static final String KEY_BROWSER_INTENT = "browserIntent";
    private static final String TAG = "CustomTabs";
    private static String oauthURL;
    private static boolean urlLoadedSuccessfully = false;

    private boolean mOpened = false;
    private static WeakReference<Activity> connectActivityRef;
    // lifecycleListener removed - CustomTabsActivityManager now notifies Connect directly

    public static Intent createStartIntent(Context context, Intent customTabsIntent, Activity activity) {
        Intent intent = createBaseIntent(context);
        intent.putExtra(KEY_BROWSER_INTENT, customTabsIntent);
        connectActivityRef = new WeakReference<>(activity);
        return intent;
    }

    // setLifecycleListener removed; Connect is notified directly via the static connectActivity reference

    public static Intent createDismissIntent(Context context) {
        Intent intent = createBaseIntent(context);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private static Intent createBaseIntent(Context context) {
        return new Intent(context, CustomTabsActivityManager.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: CustomTabsActivityManager created");

        // This activity gets opened in 2 different ways. If the extra KEY_BROWSER_INTENT is present we
        // start that intent and if it is not it means this activity was started with FLAG_ACTIVITY_CLEAR_TOP
        // in order to close the intent that was started previously so we just close this.
        if (getIntent().hasExtra(KEY_BROWSER_INTENT)) {
            Intent browserIntent = getIntent().getParcelableExtra(KEY_BROWSER_INTENT);
            if (browserIntent != null) {
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // Extract and store the URL being opened so we can log/report failures
                Uri intentData = browserIntent.getData();
                if (intentData != null) {
                    oauthURL = intentData.toString();
                }
                Log.d(TAG, "onCreate: Starting custom tabs browser intent for URL: " + (oauthURL != null ? oauthURL : "N/A"));
                try {
                    startActivity(browserIntent);
                } catch (Exception e) {
                    // Failed to start browser intent — log and notify Connect that the URL failed to load
                    Log.e(TAG, "onCreate: Failed to start custom tab/browser intent for URL: " + (oauthURL != null ? oauthURL : "N/A") + ". Error: " + e.getMessage());
                    onOAuthWebViewLoadComplete(false);
                    finish();
                    return;
                }
            } else {
                Log.w(TAG, "onCreate: Browser intent was null");
                onOAuthWebViewLoadComplete(false);
                finish();
            }
        } else {
            Log.d(TAG, "onCreate: No browser intent, finishing");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called, mOpened = " + mOpened);

        // onResume will get called twice, the first time when the activity is created and a second
        // time if the user closes the chrome tabs activity. Knowing this we can detect if the user
        // dismissed the activity and send an event accordingly.
        if (!mOpened) {
            mOpened = true;
            Log.d(TAG, "onResume: First call - Custom tab opened");
            // Notify listener that custom tab was opened

            // listener removed; Connect will be notified directly below
                // Also notify the Connect activity so it can post an OAuth-open message
                Activity stored = connectActivityRef != null ? connectActivityRef.get() : null;
                if (stored instanceof Connect) {
                    try {
                        // Track that the custom tab URL loaded successfully
                        urlLoadedSuccessfully = true;
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to post OAuth open message: " + e.getMessage());
                    }
                }
        } else {
            Log.d(TAG, "onResume: Second call - Custom tab closed by user, finishing");
            // Notify listener that custom tab was closed

            // listener removed; Connect will be notified directly below
            // Notify Connect activity so it can post an OAuth-close message
            Activity stored = connectActivityRef != null ? connectActivityRef.get() : null;
            if (stored instanceof Connect) {
                try {
                    ((Connect) stored).postWindowOauthCloseMessage(ConnectOauthCloseType.USER_CLOSED);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to post OAuth close message: " + e.getMessage());
                }
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: CustomTabsActivityManager destroyed");
        Activity stored = connectActivityRef != null ? connectActivityRef.get() : null;
        if (stored instanceof Connect && oauthURL != null) {
            Log.d(TAG, "onDestroy: Calling postWindowClosedMessage on Connect");
       }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * Track if the OAuth URL provided to CustomTabsActivityManager loaded successfully.
     * Called when the custom tab completes its initial load or encounters a load error.
     *
     * @param didLoadSuccessfully true if the custom tab URL loaded successfully, false if it failed
     */
    private void onOAuthWebViewLoadComplete(boolean didLoadSuccessfully) {
        urlLoadedSuccessfully = didLoadSuccessfully;

        Activity stored = connectActivityRef != null ? connectActivityRef.get() : null;
        if (stored instanceof Connect) {
            try {
                Log.d(TAG, "onOAuthWebViewLoadComplete: didLoadSuccessfully = " + didLoadSuccessfully + ", oauthURL = " + oauthURL);

                if (!didLoadSuccessfully) {
                    // If custom tab URL load failed, notify Connect to post blocked message
                    ((Connect) stored).postWindowBlockedMessage();
                }
            } catch (Exception e) {
                Log.w(TAG, "onOAuthWebViewLoadComplete: Failed to notify Connect: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "onOAuthWebViewLoadComplete: Connect activity not available");
        }
    }

}
