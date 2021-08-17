package com.finicity.connect.sdk;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void test01LifeCycle() {
        ActivityController<CustomTabsActivityManager> controller = Robolectric.buildActivity(CustomTabsActivityManager.class);
        CustomTabsActivityManager cta = controller.get();
        CustomTabsActivityManager spy = spy(cta);

        spy.onCreate(null);
        verify(spy).finish();
        verify(spy, times(1)).finish();
        spy.onResume();
        verify(spy, times(1)).finish();
        spy.onResume();
        verify(spy, times(2)).finish();
        spy.onNewIntent(mock(Intent.class));
        verify(spy, times(2)).finish();
//        spy.onDestroy();
//        verify(spy, times(2)).finish();
    }

    @Test
    public void test02CreateStartIntent() {
        Intent intent = CustomTabsActivityManager.createStartIntent(activity, customTabsIntent, activity);
        Assert.assertTrue(intent.hasExtra("browserIntent"));
    }

    @Test
    public void test03CreateDismissIntent() {
        Intent intent = CustomTabsActivityManager.createDismissIntent(activity);
        Assert.assertTrue((intent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0);
    }

    @Test
    public void test04OnCreateWithIntent() {
        ActivityController<CustomTabsActivityManager> controller = Robolectric.buildActivity(CustomTabsActivityManager.class);
        CustomTabsActivityManager cta = controller.get();
        CustomTabsActivityManager spy = spy(cta);

        Intent intent = CustomTabsActivityManager.createStartIntent(activity, customTabsIntent, activity);
        Assert.assertTrue(intent.hasExtra("browserIntent"));
        spy.onNewIntent(intent);
        spy.onCreate(null);
        verify(spy).startActivity(customTabsIntent);
        verify(spy, times(0)).finish();
    }

}
