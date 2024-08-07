package com.mastercard.openbanking.connect;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.web.webdriver.DriverAtoms;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.junit.Assert.fail;

import static java.sql.DriverManager.println;

import com.mastercard.openbanking.connect.generateurl.ConnectGenerateUrlCallbackHandler;
import com.mastercard.openbanking.connect.generateurl.GenUrlLib;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ConnectActivityTest {

    // Generate a 2.0 Connect url using Postman and set goodUrl to it before running UI unit tests.
    private static String goodUrl = "";
    private static final String badExpiredUrl = "https://connect2.finicity.com?consumerId=dbceec20d8b97174e6aed204856f5a55&customerId=1016927519&partnerId=2445582695152&signature=abb1762e5c640f02823c56332daede3fe2f2143f4f5b8be6ec178ac72d7dbc5a&timestamp=1607806595887&ttl=1607813795887";
    private WebEventIdlingResource mIdlingResource;
    private static String redirectUrl = "https://anydomain.com";

    @Before
    public void setup() {
        mIdlingResource = new WebEventIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
        Connect.runningUnitTest = true;
        generateConnectUrl();
    }

    @After
    public void teardown() {
        Connect.runningUnitTest = false;
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

    @Test
    public void test01ConnectWithExpiredUrl() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, redirectUrl, new TestEventHandler());
        mIdlingResource.waitForEvent("error");
        Thread.sleep(2000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());
    }

    @Test
    public void test02ConnectWithGoodUrlThenCancel() throws InterruptedException {

        String url = goodUrl.replace("localhost:", "10.0.2.2:");
        Connect.start(InstrumentationRegistry.getContext(), url, "", new TestEventHandler());
        Thread.sleep(5000);
        mIdlingResource.waitForEvent("search");
        onWebView().withElement(findElement(Locator.CLASS_NAME, "icon-nav_exit_button")).perform(webClick());

        Thread.sleep(1000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"close-modal-confirm-button\"]")).perform(webClick());

    }


    @Test
    public void test03ConnectWithGoodUrlThenBackButton() throws InterruptedException {

        String url = goodUrl.replace("localhost:", "10.0.2.2:");
        Connect.start(InstrumentationRegistry.getContext(), url, redirectUrl, new TestEventHandler());


        // Wait for landing screen and click Next
        mIdlingResource.waitForEvent("landingScreen");
        Thread.sleep(5000);
        onWebView()
                .withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "button"))
                .perform(DriverAtoms.webClick());

        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        mIdlingResource.waitForEvent( "GetInstitutionsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Try and simulate back button press to return to previous page
        mIdlingResource.waitForEvent("sign-in");
        onView(isRoot()).perform(ViewActions.pressBackUnconditionally());
    }

    @Test
    public void test04ConnectWithGoodUrlThenBackButtonAndCancel() throws InterruptedException {

        String url = goodUrl.replace("localhost:", "10.0.2.2:");
        Connect.start(InstrumentationRegistry.getContext(), url, redirectUrl,  new TestEventHandler());

        Thread.sleep(5000);
        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");

        // Try and simulate back button press to return to non-existent page to test back-button cancel event
        onView(isRoot()).perform(ViewActions.pressBackUnconditionally());
        mIdlingResource.waitForEvent("cancel");
    }

    @Test
    public void test05ConnectWithGoodUrlThenPrivacyPolicy() throws InterruptedException {

        String url = goodUrl.replace("localhost:", "10.0.2.2:");
        Connect.start(InstrumentationRegistry.getContext(), url, redirectUrl, new TestEventHandler());

        Thread.sleep(5000);
        // wait for privacy policy
        mIdlingResource.waitForEvent("privacy-policy");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Privacy Notice")).perform(webClick());

        // Try to dismiss Privacy Policy popup
        Thread.sleep(5000);
        Connect.finishCurrentActivity();
    }

    @Test
    public void test06ConnectWithGoodUrlThenAddBankAccount() throws InterruptedException {


        String url = goodUrl.replace("localhost:", "10.0.2.2:");
        Connect.start(InstrumentationRegistry.getContext(), url, redirectUrl,  new TestEventHandler());

        // Wait for landing screen
        Thread.sleep(5000);
        mIdlingResource.waitForEvent("landingScreen");
        onWebView()
                .withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "button"))
                .perform(DriverAtoms.webClick());
        Thread.sleep(3000);

        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());
        Thread.sleep(5000);

        // Select FinBank from search list using XPATH
        mIdlingResource.waitForEvent( "GetInstitutionsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        Thread.sleep(3000);
        // Click Next for Login screen
        mIdlingResource.waitForEvent("sign-in");
        onWebView()
                .withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "button"))
                .perform(DriverAtoms.webClick());

        Thread.sleep(3000);

        // Fill out UserId and Password and submit form
        mIdlingResource.waitForEvent("login");
        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Userid"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("demo"));

        Thread.sleep(2000);
        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Password"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("go"));

        Thread.sleep(2000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"container\"]/div[1]/app-institution-container/div/div[3]/app-optional-anchored-content/div/div/app-button[1]")).perform(webClick());

        // Select 1st account in list using XPATH
        Thread.sleep(10000);
        mIdlingResource.waitForEvent("DiscoverAccountsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-select-accounts\"]/div[2]/app-account-list/div/div[1]/app-account-details-card/div/div[2]/app-checkbox/label/div/div")).perform(webClick());


        // Scroll down to save button and click
        Thread.sleep(3000);
        mIdlingResource.waitForEvent("loading");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Save")).perform(webClick());

        // Click Submit button
        Thread.sleep(3000);
        mIdlingResource.waitForEvent("review-accounts");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Submit")).perform(webClick());
    }

     @Test
    public void test07ConnectWithExpiredUrlThenFinishActivity() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, redirectUrl,  new TestEventHandler());
        Thread.sleep(10000);
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

    @Test
    public void test09AlreadyRunning() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, redirectUrl, new TestEventHandler());
        Thread.sleep(5000);

        // Try and start a 2nd Activity
        try {
            Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, redirectUrl, new TestEventHandler());
            fail("Should have thrown runtime exception");
        } catch(RuntimeException e) {
            //success
        }
    }

    @Test
    public void test10NullEventHandler() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, redirectUrl,null);
        Thread.sleep(5000);
    }

    @Test
    public void test11ConnectWithGoodUrlWithoutDeeplink() throws InterruptedException {

        String url = goodUrl.replace("localhost:", "10.0.2.2:");
        Connect.start(InstrumentationRegistry.getContext(), url, new TestEventHandler());
        Thread.sleep(5000);
        mIdlingResource.waitForEvent("search");
        onWebView().withElement(findElement(Locator.CLASS_NAME, "icon-nav_exit_button")).perform(webClick());

        Thread.sleep(1000);

        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"close-modal-confirm-button\"]")).perform(webClick());

    }

    private void generateConnectUrl() {
        if (goodUrl.isEmpty()) {
            // Use countdown latch to wait for chain of Finicity API's Auth, Customer, Consumer, Generate to complete asynchronously.
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            // Try and use GenUrlLib to generate a connect url to use for tests
            GenUrlLib.generateUrl(InstrumentationRegistry.getContext(), new ConnectGenerateUrlCallbackHandler() {
                @Override
                public void onError(String error) {
                    countDownLatch.countDown();
                }

                @Override
                public void onSuccess(String link) {
                    goodUrl = link;
                    countDownLatch.countDown();
                }
            }, false);

            // Wait for generateUrl to complete with error or success
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("GenUrlAndroidTest - generateConnectUrl exiting");
        }

    }


    public class TestEventHandler implements EventHandler {
        private static final String TAG = "TestEventHandler";

        @Override
        public void onLoad() {
            Log.i(TAG, ">>> TestEventHandler: Received Loaded event");
        }

        @Override
        public void onDone(JSONObject doneEvent) {
            Log.i(TAG, ">>> TestEventHandler: Received Done event\n>>>>>> " + doneEvent.toString());
        }

        @Override
        public void onCancel(JSONObject cancelEvent) {
            Log.i(TAG, ">>> TestEventHandler: Received Cancel event\\n>>>>>> " + cancelEvent.toString());
            mIdlingResource.checkEvent("cancel");
         }

        @Override
        public void onError(JSONObject errorEvent) {
            Log.i(TAG, ">>> TestEventHandler: Received Error event\n>>>>>> " + errorEvent.toString());
        }

        @Override
        public void onRoute(JSONObject routeEvent) {
            try {
                String screenVal = routeEvent.getString("screen");
                Log.i(TAG, ">>> TestEventHandler: Received Route event\nscreen: " + screenVal);
                mIdlingResource.checkEvent(screenVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUser(JSONObject userEvent) {
            try {
                String action = userEvent.getString("action");
                Log.i(TAG, ">>> TestEventHandler: Received User event\naction: " + action);
                mIdlingResource.checkEvent(action);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class WebEventIdlingResource implements IdlingResource {
        private long startTime;
        private long waitingTime;
        private String expectedEvent;
        private boolean waitingOnExpectedEvent;
        private ResourceCallback resourceCallback;
        private final List<String> events = new ArrayList<>();

        public WebEventIdlingResource() {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = 0;
            this.expectedEvent = "";
            this.waitingOnExpectedEvent = false;
        }

        public void waitForEvent(String expectedEvent) {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = 15000;
            this.expectedEvent = expectedEvent;
            this.waitingOnExpectedEvent = true;
            internalCheckEvent();
        }

        private void internalCheckEvent() {
            // System.out.println(">>> TestEventHandler - eventStr:" + eventStr + "queuedEvents:" + events.toString());
            if (waitingOnExpectedEvent && events.contains(expectedEvent)) {
                events.remove(expectedEvent);
                waitingOnExpectedEvent = false;
                expectedEvent = "";
                waitingTime = 0;
                resourceCallback.onTransitionToIdle();
            }
        }

        public void checkEvent(String event) {
            events.add(event);
            internalCheckEvent();
        }

        @Override
        public String getName() {
            return ConnectActivityTest.class.getName() + ":" + waitingTime;
        }

        @Override
        public boolean isIdleNow() {
            long elapsed = System.currentTimeMillis() - startTime;
            boolean idle = (elapsed >= waitingTime);
            if (idle) {
                waitingOnExpectedEvent = false;
                expectedEvent = "";
                waitingTime = 0;
                resourceCallback.onTransitionToIdle();
            }
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(IdlingResource.ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

}
