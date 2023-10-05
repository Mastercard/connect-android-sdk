package com.mastercard.openbanking.connect;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class ConnectWebViewClientTest {

    @Mock
    private ConnectWebViewClientHandler connectWebViewClientHandler;
    @Mock
    private WebView webView;

    @Mock
    private ProgressBar progressBar;

    private ConnectWebViewClient webViewClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        webViewClient = new ConnectWebViewClient(connectWebViewClientHandler);

    }

    @Test
    public void testOnPageFinished01() {
        // Simulate onPageFinished callback
        webViewClient.onPageFinished(webView, "https://example.com");

        // Verify that the progress bar becomes GONE
//        verify(progressBar).setVisibility(View.GONE);
        verify(connectWebViewClientHandler).handleOnPageFinish();
    }

    @Test
    public void testOnReceivedError() {
        // Simulate onReceivedError callback
        WebResourceRequest request = mock(WebResourceRequest.class);
        WebResourceError error = mock(WebResourceError.class);

        // Call the onReceivedError method
        webViewClient.onReceivedError(webView, request, error);

        // Verify that the ErrorHandler's handleBadURLError method is called
        verify(connectWebViewClientHandler).handleBadURLError();
    }

    @Test
    public void testHandleWebviewInitialLoading() {

        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        Connect cta = controller.get();
        Connect spy = spy(cta);

        cta.handleWebviewInitialLoading(webView, connectWebViewClientHandler);

        // Debugging: Check if setWebViewClient is called with the expected argument
        ArgumentCaptor<WebViewClient> webViewClientCaptor = ArgumentCaptor.forClass(WebViewClient.class);
        verify(webView).setWebViewClient(webViewClientCaptor.capture());

        assertTrue(webViewClientCaptor.getValue() instanceof ConnectWebViewClient);

    }

    @Test
    public void testHandleBadURLError() {


        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        Connect cta = controller.get();
        Connect spy = spy(cta);
        spy.handleBadURLError();
        verify(spy,times(1)).finish();
    }


}
