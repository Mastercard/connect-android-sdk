package com.finicity.connect.sdk;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class ConnectJsInterfaceTest {

    private Activity activity;
    private EventListener eventListener;

    private ConnectJsInterface jsInterface;

    @Before
    public void setup() {
        // Mock activity and eventlistener
        activity = mock(Activity.class);
        eventListener = mock(EventListener.class);

        jsInterface = new ConnectJsInterface(activity, eventListener);
    }

    @Test
    public void testPostMessage_invalidJson() {
        String junkMessage = "this is not JSON !!!!";

        jsInterface.postMessage(junkMessage);

        verifyZeroInteractions(eventListener);
        verifyZeroInteractions(activity);
    }

    @Test
    public void testPostMessage_noEventType() {
        String junkMessage = "{ \"noType\": \"no type field in this message\"}";

        jsInterface.postMessage(junkMessage);

        verifyZeroInteractions(eventListener);
        verifyZeroInteractions(activity);
    }

    @Test
    public void testPostMessage_unrecognizedEventType() {
        String invalidTypeMsg = "{ \"type\": \"unrecognized\" }";

        jsInterface.postMessage(invalidTypeMsg);

        verifyZeroInteractions(eventListener);
        verifyZeroInteractions(activity);
    }

    @Test
    public void testPostMessage_cancelEvent() {
        String message = "{ \"type\": \"cancel\" }";

        jsInterface.postMessage(message);

        verify(eventListener).onCancel();
        verify(eventListener, never()).onDone(any(JSONObject.class));
        verify(eventListener, never()).onError(any(JSONObject.class));
        verify(eventListener, never()).onLoaded();

        verify(activity).finish();
    }

    @Test
    public void testPostMessage_doneEvent() {
        String message = "{ \"type\": \"done\" }";

        jsInterface.postMessage(message);

        verify(eventListener).onDone(any(JSONObject.class));
        verify(eventListener, never()).onCancel();
        verify(eventListener, never()).onError(any(JSONObject.class));
        verify(eventListener, never()).onLoaded();

        verify(activity).finish();
    }

    @Test
    public void testPostMessage_errorEvent() {
        String message = "{ \"type\": \"error\" }";

        jsInterface.postMessage(message);

        verify(eventListener).onError(any(JSONObject.class));
        verify(eventListener, never()).onDone(any(JSONObject.class));
        verify(eventListener, never()).onCancel();
        verify(eventListener, never()).onLoaded();

        verify(activity).finish();
    }

    @Test
    public void testOpenLinkInBrowser() {
        jsInterface.openLinkInBrowser("url");

        verify(activity).startActivity(any(Intent.class));
    }
}
