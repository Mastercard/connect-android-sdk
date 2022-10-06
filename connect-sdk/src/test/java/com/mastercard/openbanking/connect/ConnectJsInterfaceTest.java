package com.mastercard.openbanking.connect;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class ConnectJsInterfaceTest {

    private Activity activity;
    private Connect connActivity;
    private EventHandler eventHandler;
    private ConnectJsInterface jsInterface;

    @Before
    public void setup() {
        // Mock activity and eventlistener
        activity = mock(Connect.class);
        connActivity = (Connect) activity;
        eventHandler = mock(EventHandler.class);
        jsInterface = new ConnectJsInterface(activity, eventHandler);
    }

    @Test
    public void testPostMessage_invalidJson() {
        String junkMessage = "this is not JSON !!!!";

        jsInterface.postMessage(junkMessage);

        verifyZeroInteractions(eventHandler);
        verifyZeroInteractions(activity);
    }

    @Test
    public void testPostMessage_noEventType() {
        String junkMessage = "{ \"noType\": \"no type field in this message\"}";

        jsInterface.postMessage(junkMessage);

        verifyZeroInteractions(eventHandler);
        verifyZeroInteractions(activity);
    }

    @Test
    public void testPostMessage_unrecognizedEventType() {
        String invalidTypeMsg = "{ \"type\": \"unrecognized\" }";

        jsInterface.postMessage(invalidTypeMsg);

        verifyZeroInteractions(eventHandler);
        verifyZeroInteractions(activity);
    }

    @Test
    public void testPostMessage_cancelEvent() {
        String message = "{ \"type\": \"cancel\" }";

        jsInterface.postMessage(message);

        verify(eventHandler).onCancel(any(JSONObject.class));
        verify(eventHandler, never()).onDone(any(JSONObject.class));
        verify(eventHandler, never()).onError(any(JSONObject.class));
        verify(eventHandler, never()).onLoad();

        verify(activity).finish();
    }

    @Test
    public void testPostMessage_doneEvent() {
        String message = "{ \"type\": \"done\" }";

        jsInterface.postMessage(message);

        verify(eventHandler).onDone(any(JSONObject.class));
        verify(eventHandler, never()).onCancel(any(JSONObject.class));
        verify(eventHandler, never()).onError(any(JSONObject.class));
        verify(eventHandler, never()).onLoad();

        verify(activity).finish();
    }

    @Test
    public void testPostMessage_errorEvent() {
        String message = "{ \"type\": \"error\" }";

        jsInterface.postMessage(message);

        verify(eventHandler).onError(any(JSONObject.class));
        verify(eventHandler, never()).onDone(any(JSONObject.class));
        verify(eventHandler, never()).onCancel(any(JSONObject.class));
        verify(eventHandler, never()).onLoad();

        verify(activity).finish();
    }

    @Test
    public void testPostMessage_routeEvent() {
        String message = "{ \"type\": \"route\" }";

        jsInterface.postMessage(message);

        verify(eventHandler).onRoute(any(JSONObject.class));
    }

    @Test
    public void testPostMessage_userEvent() {
        String message = "{ \"type\": \"user\" }";

        jsInterface.postMessage(message);

        verify(eventHandler).onUser(any(JSONObject.class));
    }

    @Test
    public void testPostMessage_closePopup() {
        String message = "{ \"type\": \"closePopup\" }";

        jsInterface.postMessage(message);

        verify(activity, times(0)).startActivity(any(Intent.class));
    }

    @Test
    public void testPostMessage_ack() {
        String message = "{ \"type\": \"ack\" }";

        jsInterface.postMessage(message);

        verify(connActivity, atLeast(1)).stopPingTimer();
    }

    @Test
    public void testCloseCustomTab() {
        jsInterface.closeCustomTab();

        verify(activity, times(0)).startActivity(any(Intent.class));
    }

    @Test
    public void testOpenCustomTab() {
        jsInterface.openLinkInCustomTab("url");

        verify(activity, times(1)).startActivity(any(Intent.class));
    }

    @Test
    public void testOpenCloseCustomTab() {
        jsInterface.openLinkInCustomTab("url");
        verify(activity, times(1)).startActivity(any(Intent.class));

        jsInterface.closeCustomTab();
        verify(activity, times(2)).startActivity(any(Intent.class));
    }

    @Test
    public void testPostMessage_url() {
        String message = "{ \"type\": \"url\", \"url\": \"url\" }";
        jsInterface.postMessage(message);
        verify(activity, times(1)).startActivity(any(Intent.class));
    }
}
