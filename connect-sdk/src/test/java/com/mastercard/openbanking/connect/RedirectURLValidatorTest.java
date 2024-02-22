package com.mastercard.openbanking.connect;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import android.os.Build;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class RedirectURLValidatorTest {

    Connect connect;
    @Before
    public void setup() {
        // Mock activity and
        ActivityController<Connect> controller = Robolectric.buildActivity(Connect.class);
        connect = controller.get();
    }
    @Test
    public void testValidredirectURL() {

        assertTrue(connect.isValidUrl("myapp://"));
        assertTrue(connect.isValidUrl("myapp://linktoapp"));
        assertTrue(connect.isValidUrl("http://example.com"));
        assertTrue(connect.isValidUrl("myapp://subdomain.example.com"));
        assertTrue(connect.isValidUrl("myapp://path/path1"));
        assertTrue(connect.isValidUrl("ftp://example.com"));
        assertTrue(connect.isValidUrl("https://acmelending.net/"));
        assertTrue(connect.isValidUrl("https://acme.stg.fini.city/"));

    }
    @Test
    public void testInvalidRedirectURL() {

        assertFalse(connect.isValidUrl("linktodomain"));
        assertFalse(connect.isValidUrl("http://"));
        assertFalse(connect.isValidUrl("myapp:"));
        assertFalse(connect.isValidUrl("acmelending.net"));
        assertFalse(connect.isValidUrl("invalid-url"));
        assertFalse(connect.isValidUrl("http://www.ex@mple.com"));
        assertFalse(connect.isValidUrl("http://www.ex  ample.com"));
        assertFalse(connect.isValidUrl("http://www.example.com/page?name=John & id=123m"));
        assertFalse(connect.isValidUrl("http/www.example.com"));
        assertFalse(connect.isValidUrl("http:// instead of http://"));
        assertFalse(connect.isValidUrl("file:// instead of file://"));
    }
}
