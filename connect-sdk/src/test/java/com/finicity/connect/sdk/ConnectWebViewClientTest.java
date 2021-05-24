package com.finicity.connect.sdk;

import android.app.Activity;
import android.webkit.WebView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ConnectWebViewClientTest {

    private Activity activity;
    private EventHandler eventHandler;
    private static String CONNECT_URL = "http://host?param=val";

    private ConnectWebViewClient client;

    @Before
    public void setup() {
        activity = mock(Connect.class);
        eventHandler = mock(EventHandler.class);
        client = new ConnectWebViewClient((Connect) activity, eventHandler, CONNECT_URL);
    }

    @Test
    public void testOnPageFinished_matchingUrl() {
        // Replace '?' with '/?' which is the expected Android behavior on this callback
        String url = CONNECT_URL.replace("?", "/?");

        client.onPageFinished(mock(WebView.class), url);

        verify(eventHandler).onLoaded();
    }

    @Test
    public void testOnPageFinished_nonMatchingUrl() {
        String url = "not a match";

        client.onPageFinished(mock(WebView.class), url);

        verify(eventHandler, never()).onLoaded();
    }
}
