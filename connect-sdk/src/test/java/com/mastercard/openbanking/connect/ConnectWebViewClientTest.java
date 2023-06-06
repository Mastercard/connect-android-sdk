package com.mastercard.openbanking.connect;

import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
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
    public void testShouldIntercept_matchingUrl() {
        WebResourceRequest webResourceRequest = mock(WebResourceRequest.class);
        when(webResourceRequest.getUrl()).thenReturn(Uri.parse("config.json"));
        client.shouldInterceptRequest(mock(WebView.class), webResourceRequest);

        verify(eventHandler).onLoad();
    }

    @Test
    public void testShouldIntercept_nonMatchingUrl() {
        WebResourceRequest webResourceRequest = mock(WebResourceRequest.class);
        when(webResourceRequest.getUrl()).thenReturn(Uri.parse("test"));
        client.shouldInterceptRequest(mock(WebView.class), webResourceRequest);
        verify(eventHandler, never()).onLoad();
    }
}
