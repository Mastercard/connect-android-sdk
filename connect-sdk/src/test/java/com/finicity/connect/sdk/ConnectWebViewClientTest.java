package com.finicity.connect.sdk;

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

    private EventHandler eventHandler;
    private ConnectWebViewClient client;
    private static final String CONNECT_URL = "http://host?param=val";

    @Before
    public void setup() {
        Connect activity = mock(Connect.class);
        eventHandler = mock(EventHandler.class);
        client = new ConnectWebViewClient(activity, eventHandler, CONNECT_URL);
    }

    @Test
    public void testOnPageFinished_matchingUrl() {
        // Replace '?' with '/?' which is the expected Android behavior on this callback
        String url = CONNECT_URL.replace("?", "/?");

        client.onPageFinished(mock(WebView.class), url);

        verify(eventHandler).onLoad();
    }

    @Test
    public void testOnPageFinished_nonMatchingUrl() {
        String url = "not a match";

        client.onPageFinished(mock(WebView.class), url);

        verify(eventHandler, never()).onLoad();
    }
}
