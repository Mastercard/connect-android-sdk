package com.finicity.connect.sdk;


import android.app.Activity;
import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class CustomTabsActivityManagerTest {

    private Activity activity;
    private Intent customTabsIntent;

    @Before
    public void setup() {
        activity = mock(Activity.class);
        customTabsIntent = mock(Intent.class);
    }

    @Test
    public void testCreateStartIntent() {
        Intent intent = CustomTabsActivityManager.createStartIntent(activity, customTabsIntent, activity);
        Assert.assertTrue(intent.hasExtra("browserIntent"));
    }

    @Test
    public void testCreateDismissIntent() {
        Intent intent = CustomTabsActivityManager.createDismissIntent(activity);
        Assert.assertTrue((intent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0);
    }
}
