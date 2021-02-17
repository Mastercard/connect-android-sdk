package com.finicity.connect.sdk;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isJavascriptEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ConnectActivityTest {

    // Generate a 2.0 Connect url using Postman and set goodUrl to it before running UI unit tests.
    private static final String goodUrl = "https://connect2.finicity.com/?consumerId=274552dfe090bccf5ce3e735d4ef51eb&customerId=1017865679&partnerId=2445582695152&signature=deadd0a331a90753dcd91d7da407d03ae5489b7784200a4ebded572da01ab608&timestamp=1612391678803&ttl=1614983678803&webhook=https%3A%2F%2Fwebhook.site%2F9f34fa76-f542-4785-a35c-fd4d2d57b1d2";
    private static final String badExpiredUrl = "https://connect2.finicity.com?consumerId=dbceec20d8b97174e6aed204856f5a55&customerId=1016927519&partnerId=2445582695152&redirectUri=http%3A%2F%2Flocalhost%3A3001%2Fcustomers%2FredirectHandler&signature=abb1762e5c640f02823c56332daede3fe2f2143f4f5b8be6ec178ac72d7dbc5a&timestamp=1607806595887&ttl=1607813795887";

    @Before
    public void initTest() {
        Connect.runningUnitTest = true;
    }

    @After
    public void teardown() {
        Connect.runningUnitTest = false;
    }

    @Test
    public void test01ConnectWithExpiredUrl() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventHandler());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());
    }

    @Test
    public void test02ConnectWithGoodUrlThenCancel() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test03ConnectWithGoodUrlThenBackButton() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        Thread.sleep(5000);
        // Search for FinBank
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // This seems pretty fragile to use XPATH but it seems to work in selecting FinBank
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Try and simulate back button press to return to previous page
        Thread.sleep(5000);
        onView(isRoot()).perform(ViewActions.pressBack());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test04ConnectWithGoodUrlThenPrivacyPolicy() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        Thread.sleep(5000);
        // Search for FinBank
        onWebView()
            .withElement(findElement(Locator.NAME, "Search for your bank"))
            .perform(DriverAtoms.clearElement())
            .perform(DriverAtoms.webKeys("FinBank"))
            .perform(webClick());

        // This seems pretty fragile to use XPATH but it seems to work
        //*[@id="institution-search"]/div/div/div[1]/div
        Thread.sleep(5000);
        // onWebView().withElement(findElement(Locator.XPATH, "/html/body/app-root/div/div/div[2]/div/app-search/div/div/div/div/app-institution-list/div/div/div/div[1]/div")).perform(webClick());
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Privacy policy")).perform(webClick());

        // Try to dismiss Privacy Policy popup
        Thread.sleep(5000);
        onView(withId(R.id.popupCloseTextButton)).perform(click());

        Thread.sleep(5000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(5000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test05ConnectWithGoodUrlThenPrivacyPolicyThenBackButton() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        Thread.sleep(5000);
        // Search for FinBank
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // This seems pretty fragile to use XPATH but it seems to work
        //*[@id="institution-search"]/div/div/div[1]/div
        Thread.sleep(5000);
        // onWebView().withElement(findElement(Locator.XPATH, "/html/body/app-root/div/div/div[2]/div/app-search/div/div/div/div/app-institution-list/div/div/div/div[1]/div")).perform(webClick());
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Privacy policy")).perform(webClick());

        // Perform back button press to dismiss popup and display dialog to user
        Thread.sleep(5000);
        onView(isRoot()).perform(ViewActions.pressBack());

        // Dismiss dialog with yes
        Thread.sleep(5000);
        onView(withId(android.R.id.button1)).perform(ViewActions.click());

        Thread.sleep(5000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(5000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test06ConnectWithGoodUrlThenAddBankAccount() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        Thread.sleep(5000);
        // Search for FinBank
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Click Continue using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"financial-sign-in\"]/div[2]/app-button/a/div")).perform(webClick());

        // Fill out UserId and Password
        Thread.sleep(5000);
        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Userid"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("demo"));

        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Password"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("go"));

        // Click Sign In using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-login\"]/form/app-button/a")).perform(webClick());

        // Select 1st account in list using XPATH
        Thread.sleep(10000);
        //*[@id="institution-select-accounts"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-select-accounts\"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div")).perform(webClick());

        // Scroll down to save button and click
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Save")).perform(webScrollIntoView()).perform(webClick());

        // Click Submit button
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Submit")).perform(webClick());
    }

     @Test
    public void test07ConnectWithExpiredUrlThenFinishActivity() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventHandler());

        Thread.sleep(5000);
        Connect.finishCurrentActivity();
    }

    @Test
    public void test08FinishActivity() {

        // Try and finish a Activity that was never started
        try {
            Connect.finishCurrentActivity();
            fail("Should have thrown runtime exception");
        } catch(RuntimeException e) {
            //success
        }
    }

    // Tests 09, 10, 11 use deprecated TestEventListener
    @Test
    public void test09ConnectWithExpiredUrl() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventListener());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());
    }

    @Test
    public void test10ConnectWithGoodUrlThenCancel() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventListener());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test11ConnectWithGoodUrlThenAddBankAccount() throws InterruptedException {

        if (goodUrl.length() == 0) {
            fail(">>> CONNECT ERROR: Need to generate a Connect 2.0 url and set goodUrl to url before running test!!!");
        }

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventListener());

        Thread.sleep(5000);
        // Search for FinBank
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Click Continue using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"financial-sign-in\"]/div[2]/app-button/a/div")).perform(webClick());

        // Fill out UserId and Password
        Thread.sleep(5000);
        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Userid"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("demo"));

        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Password"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("go"));

        // Click Sign In using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-login\"]/form/app-button/a")).perform(webClick());

        // Select 1st account in list using XPATH
        Thread.sleep(10000);
        //*[@id="institution-select-accounts"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-select-accounts\"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div")).perform(webClick());

        // Scroll down to save button and click
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Save")).perform(webScrollIntoView()).perform(webClick());

        // Click Submit button
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Submit")).perform(webClick());
    }

    public class TestEventHandler implements EventHandler {
        @Override
        public void onLoaded() {
            System.out.println(">>> TestEventHandler: Received loaded event");
        }

        @Override
        public void onDone(JSONObject doneEvent) {
            System.out.println(">>> TestEventHandler: Received done event\n>>>>>> " + doneEvent.toString());
        }

        @Override
        public void onCancel() {
            System.out.println(">>> TestEventHandler: Received Cancel event");
        }

        @Override
        public void onError(JSONObject errorEvent) {
            System.out.println(">>> TestEventHandler: Received Error event\n>>>>>> " + errorEvent.toString());
        }

        @Override
        public void onRouteEvent(JSONObject routeEvent) {
            System.out.println(">>> TestEventHandler: Received Route event\n>>>>>> " + routeEvent.toString());
        }

        @Override
        public void onUserEvent(JSONObject userEvent) {
            System.out.println(">>> TestEventHandler: Received User event\n>>>>>> " + userEvent.toString());

        }
    }

    public class TestEventListener implements EventListener {
        @Override
        public void onLoaded() {
            System.out.println(">>> TestEventListener: Received loaded event");
        }

        @Override
        public void onDone(JSONObject doneEvent) {
            System.out.println(">>> TestEventListener: Received done event\n>>>>>> " + doneEvent.toString());
        }

        @Override
        public void onCancel() {
            System.out.println(">>> TestEventListener: Received Cancel event");
        }

        @Override
        public void onError(JSONObject errorEvent) {
            System.out.println(">>> TestEventListener: Received Error event\n>>>>>> " + errorEvent.toString());
        }
    }

}
