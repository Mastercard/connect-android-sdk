package com.finicity.connect.demo;



import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.finicity.connect.sdk.Connect;
import com.finicity.connect.sdk.EventHandler;
import com.finicity.connect.sdk.EventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mStartButtonEventListener;
    private Button mStartButtonEventHandler;
    private EditText mEditConnectUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add click listeners for EventListener and EventHandler buttons
        this.mStartButtonEventListener = findViewById(R.id.startButtonEventListener);

        mStartButtonEventListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(new ConsoleEventListener());
            }
        });

        this.mStartButtonEventHandler = findViewById(R.id.startButtonEventHandler);

        mStartButtonEventHandler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(new ConsoleEventHandler());
            }
        });

        this.mEditConnectUrl = findViewById(R.id.editConnectUrl);
    }

    private void launchActivity(EventListener listener) {
        String url = getEditConnectUrl();

        if(url.length() > 0) {
            // Null out text so we can repeat with new link after Connect Activity closes.
            mEditConnectUrl.setText("");

            Log.i(TAG, ">>> Launching Connect activity");

            Connect.start(this, url, listener);
        }
    }

    private void launchActivity(EventHandler eventHandler) {
        String url = getEditConnectUrl();

        if(url.length() > 0) {
            // Null out text so we can repeat with new link after Connect Activity closes.
            mEditConnectUrl.setText("");

            Log.i(TAG, ">>> Launching Connect activity");

            Connect.start(this, url, eventHandler);
        }
    }

    private String getEditConnectUrl() {
        String rawUrl = this.mEditConnectUrl.getText().toString();

        return rawUrl.replace("localhost:", "10.0.2.2:");
    }
}
