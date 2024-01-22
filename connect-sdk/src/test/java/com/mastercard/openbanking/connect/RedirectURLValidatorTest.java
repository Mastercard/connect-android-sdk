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

import android.net.Uri;
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
    public void testValidDeepLink() {

        assertTrue(connect.isValidRedirectUrl("myapp://"));
        assertTrue(connect.isValidRedirectUrl("myapp://linktoapp"));
        assertTrue(connect.isValidRedirectUrl("http://example.com"));
        assertTrue(connect.isValidRedirectUrl("myapp://subdomain.example.com"));
        assertTrue(connect.isValidRedirectUrl("myapp://path/path1"));
        assertTrue(connect.isValidRedirectUrl("ftp://example.com"));
        assertTrue(connect.isValidRedirectUrl("https://acmelending.net/"));
        assertTrue(connect.isValidRedirectUrl("https://acme.stg.fini.city/"));
        assertTrue(connect.isValidRedirectUrl("")); //returns true because this is an optional parameter
        assertTrue(connect.isValidRedirectUrl(null)); //returns true because this is an optional parameter
    }
    @Test
    public void testInvalidDeepLink() {

        assertFalse(connect.isValidRedirectUrl("linktodomain"));
        assertFalse(connect.isValidRedirectUrl("myapp:"));
        assertFalse(connect.isValidRedirectUrl("acmelending.net"));
        assertFalse(connect.isValidRedirectUrl("invalid-url"));
        assertFalse(connect.isValidRedirectUrl("invalid url"));
    }
}
