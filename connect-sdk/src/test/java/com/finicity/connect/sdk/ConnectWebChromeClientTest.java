package com.finicity.connect.sdk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner.class)
public class ConnectWebChromeClientTest {

    @Test
    public void test01OnShowFileChooser() {

        // Test trying to start a new File Picker Activity
        Connect connect = mock(Connect.class);

        ValueCallback<Uri[]> mFilePathCallback = new ValueCallback<Uri[]>() {
            @Override
            public void onReceiveValue(Uri[] value) {
            }
        };

        ConnectWebChromeClient connectWebChromeClient = spy(new ConnectWebChromeClient(connect, null, null, null, null));

        WebView webView = mock(WebView.class);
        WebChromeClient.FileChooserParams params = spy(WebChromeClient.FileChooserParams.class);
        doReturn(mock(Intent.class)).when(params).createIntent();
        connectWebChromeClient.onShowFileChooser(webView, mFilePathCallback, params);

        Assert.assertNotNull(connect.mFilePathCallback);
    }

    @Test
    public void test02OnShowFileChooser() {

        // Test where Connect activity already has mFilePathCallback set when trying to add a new File Picker Activity
        Connect connect = mock(Connect.class);

        ValueCallback<Uri[]> mFilePathCallback = new ValueCallback<Uri[]>() {
            @Override
            public void onReceiveValue(Uri[] value) {
            }
        };

        connect.mFilePathCallback = mFilePathCallback;

        ConnectWebChromeClient connectWebChromeClient = spy(new ConnectWebChromeClient(connect, null, null, null, null));

        WebView webView = mock(WebView.class);
        WebChromeClient.FileChooserParams params = spy(WebChromeClient.FileChooserParams.class);
        doReturn(mock(Intent.class)).when(params).createIntent();
        connectWebChromeClient.onShowFileChooser(webView, mFilePathCallback, params);

        Assert.assertNotNull(connect.mFilePathCallback);
    }

    @Test
    public void test03OnShowFileChooserThrowException() {

        // Test throwing exception ActivityNotFoundException
        Connect connect = mock(Connect.class);
        doThrow(ActivityNotFoundException.class).when(connect).startActivityForResult(any(Intent.class), eq(Connect.SELECT_FILE_RESULT_CODE));

        ValueCallback<Uri[]> mFilePathCallback = new ValueCallback<Uri[]>() {
            @Override
            public void onReceiveValue(Uri[] value) {
            }
        };

        ConnectWebChromeClient connectWebChromeClient = spy(new ConnectWebChromeClient(connect, null, null, null, null));
        ConnectWebChromeClient.runningUnitTest = true;

        WebView webView = mock(WebView.class);
        WebChromeClient.FileChooserParams params = spy(WebChromeClient.FileChooserParams.class);
        doReturn(mock(Intent.class)).when(params).createIntent();
        connectWebChromeClient.onShowFileChooser(webView, mFilePathCallback, params);

        Assert.assertNull(connect.mFilePathCallback);
    }

    @Test
    public void test04OnActivityResult() {

        // Test Connect.onActivityResult being called with request code != Connect.SELECT_FILE_RESULT_CODE
        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        Connect cta = controller.get();
        Connect spy = spy(cta);

        spy.mFilePathCallback = new ValueCallback<Uri[]>() {
            @Override
            public void onReceiveValue(Uri[] value) {
            }
        };

        Intent intent = mock(Intent.class);
        spy.onActivityResult(Connect.SELECT_FILE_RESULT_CODE+1, Activity.RESULT_OK,intent);
        Assert.assertNotNull(spy.mFilePathCallback);
    }

    @Test
    public void test05OnActivityResult() {

        // Test Connect.onActivityResult being called with RESULT_CANCELED
        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        Connect cta = controller.get();
        Connect spy = spy(cta);

        spy.mFilePathCallback = new ValueCallback<Uri[]>() {
            @Override
            public void onReceiveValue(Uri[] value) {
            }
        };

        Intent intent = mock(Intent.class);
        spy.onActivityResult(Connect.SELECT_FILE_RESULT_CODE,Activity.RESULT_CANCELED,intent);
        Assert.assertNull(spy.mFilePathCallback);
    }

    @Test
    public void test06OnActivityResult() {

        // Test Connect.onActivityResult being called with RESULT_OK but mFilePathCallback is null
        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        Connect cta = controller.get();
        Connect spy = spy(cta);
        Intent intent = mock(Intent.class);
        spy.onActivityResult(Connect.SELECT_FILE_RESULT_CODE,Activity.RESULT_OK,intent);
        Assert.assertNull(spy.mFilePathCallback);
    }

    @Test
    public void test07OnActivityResult() {

        // Test Connect.onActivityResult being called with RESULT_OK and mFilePathCallback is not null
        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        Connect cta = controller.get();
        Connect spy = spy(cta);

        spy.mFilePathCallback = new ValueCallback<Uri[]>() {
            @Override
            public void onReceiveValue(Uri[] value) {
            }
        };

        Intent intent = mock(Intent.class);

        MockedStatic<WebChromeClient.FileChooserParams> theMock = Mockito.mockStatic(WebChromeClient.FileChooserParams.class);
        theMock.when(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                WebChromeClient.FileChooserParams.parseResult(anyInt(), any(Intent.class));
            }
        }).thenReturn(null);

        spy.onActivityResult(Connect.SELECT_FILE_RESULT_CODE,Activity.RESULT_OK,intent);
        Assert.assertNull(spy.mFilePathCallback);
    }

}
