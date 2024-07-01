package com.mastercard.openbanking.connect.generateurl;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.mastercard.openbanking.connect.R;

import java.util.concurrent.CountDownLatch;

public class GenUrlLib {

    // private static final String TAG = MainActivity.class.getName();
    private static final String TAG = "GenUrl-Lib";
    private static RequestQueue mRequestQueue;
    private static Context context;

    private static Response.ErrorListener errListener;
    private static Response.Listener<ConnectAuthResponse> authListener;
    private static Response.Listener<ConnectCustomerResponse> customerListener;
    private static Response.Listener<ConnectConsumerResponse> consumerListener;
    private static Response.Listener<ConnectGenerateUrlResponse> generateUrlListener;

    public static void generateUrl(final Context context, final ConnectGenerateUrlCallbackHandler callbackHandler, final Boolean bRunOnUIThread) {

        // Reset static variables
        mRequestQueue = null;
        errListener = null;
        authListener = null;
        customerListener = null;
        consumerListener = null;
        generateUrlListener = null;
        GenUrlLib.context = context;

        // Send network requests from background task.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                generateUrlInBackground(context, callbackHandler, bRunOnUIThread);
                return null;
            }
        }.execute();
    }

    private static void generateUrlInBackground(Context context, final ConnectGenerateUrlCallbackHandler callbackHandler, Boolean bRunOnUIThread) {
        mRequestQueue = Volley.newRequestQueue(context);

        // Use countdown latch to wait for chain of Finicity API's Auth, Customer, Consumer, Generate to complete asynchronously.
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        // This will store either volley error, or generated url.
        final Object[] responseHolder = new Object[1];

        errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMsg = error.getMessage();
                Log.i(TAG, "error msg :" + errorMsg);
                Log.i(TAG, "error :" + error.toString());
                responseHolder[0] = error;
                // On error unblock wait
                countDownLatch.countDown();
            }
        };

        authListener = new Response.Listener<ConnectAuthResponse>() {
            @Override
            public void onResponse(ConnectAuthResponse response) {
                Log.i(TAG, "auth token: " + response.token);
                ConnectConfig.authToken = response.token;
                sendCustomerRequest();
            }
        };

        customerListener = new Response.Listener<ConnectCustomerResponse>() {
            @Override
            public void onResponse(ConnectCustomerResponse response) {
                Log.i(TAG, "customer id: " + response.id);
                ConnectConfig.customerId = response.id;
                sendConsumerRequest();
            }
        };

        consumerListener = new Response.Listener<ConnectConsumerResponse>() {
            @Override
            public void onResponse(ConnectConsumerResponse response) {
                Log.i(TAG, "consumer id: " + response.id);
                ConnectConfig.consumerId = response.id;
                sendGenerateUrlRequest();
            }
        };

        generateUrlListener = new Response.Listener<ConnectGenerateUrlResponse>() {
            @Override
            public void onResponse(ConnectGenerateUrlResponse response) {
                Log.i(TAG, "generate url link: " + response.link);
                responseHolder[0] = response.link;
                // on success unblock wait
                countDownLatch.countDown();
            }
        };

        // Start the chain of requests to Finicity endpoints
        sendAuthRequest();

        // Wait for chain of requests to either complete or receive error
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (responseHolder[0] instanceof VolleyError) {
            final VolleyError volleyError = (VolleyError) responseHolder[0];
            if (!bRunOnUIThread) {
                callbackHandler.onError(volleyError.getMessage());
            } else {
                Activity activity = (Activity) context;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callbackHandler.onError(volleyError.getMessage());
                    }
                });
            }
        } else if (responseHolder[0] instanceof String) {
            final String response = (String) responseHolder[0];
            if (!bRunOnUIThread) {
                callbackHandler.onSuccess(response);
            } else {
                Activity activity = (Activity) context;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callbackHandler.onSuccess(response);
                    }
                });
            }
        }
    }

    private static void sendAuthRequest() {

        // Create json params
        ConnectAuthRequest req = new ConnectAuthRequest(context.getResources().getString(R.string.partnerId), context.getResources().getString(R.string.partnerSecret));
        // Send Request
        GsonRequest<ConnectAuthResponse> myReq = new GsonRequest<>(
                context,
                Request.Method.POST,
                ConnectConfig.AUTHENTICATE_ENDPOINT,
                req,
                ConnectAuthResponse.class,
                authListener,
                errListener );

        mRequestQueue.add(myReq);
        myReq.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private static void sendCustomerRequest() {

        // Create json params
        long tsLong = System.currentTimeMillis()/1000;
        String timestamp = Long.toString(tsLong);
        ConnectCustomerRequest req = new ConnectCustomerRequest(timestamp, "John", "Doe");

        // Send Request
        GsonRequest<ConnectCustomerResponse> myReq = new GsonRequest<>(
                context,
                Request.Method.POST,
                ConnectConfig.CUSTOMER_ENDPOINT,
                req,
                ConnectCustomerResponse.class,
                customerListener,
                errListener );

        mRequestQueue.add(myReq);
        myReq.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private static void sendConsumerRequest() {

        // Create json params
        ConnectConsumerRequest req =
                new ConnectConsumerRequest(
                        "John",
                        "Smith",
                        "Some address",
                        "Some City",
                        "UT",
                        "84109",
                        "888-888-8888",
                        "555555555",
                        1989,
                        8,
                        13);

        String endpoint = "https://api.finicity.com/decisioning/v1/customers/" + ConnectConfig.customerId + "/consumer";

        // Send Request
        GsonRequest<ConnectConsumerResponse> myReq = new GsonRequest<>(
                context,
                Request.Method.POST,
                endpoint,
                req,
                ConnectConsumerResponse.class,
                consumerListener,
                errListener );

        mRequestQueue.add(myReq);
        myReq.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private static void sendGenerateUrlRequest() {

        // Create json params
        ConnectGenerateUrlRequest req = new ConnectGenerateUrlRequest(context.getResources().getString(R.string.partnerId), ConnectConfig.customerId, ConnectConfig.consumerId);
        // Send Request
        GsonRequest<ConnectGenerateUrlResponse> myReq = new GsonRequest<>(
                context,
                Request.Method.POST,
                ConnectConfig.GENERATE_URL_ENDPOINT,
                req,
                ConnectGenerateUrlResponse.class,
                generateUrlListener,
                errListener );

        mRequestQueue.add(myReq);
        myReq.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}

