package com.mastercard.openbanking.connect.demo;



import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mastercard.openbanking.connect.Connect;
import com.mastercard.openbanking.connect.EventHandler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText mEditConnectUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add click listener for EventListener
        Button mStartButtonEventHandler = findViewById(R.id.startButtonEventHandler);

        mStartButtonEventHandler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(new ConsoleEventHandler());
            }
        });

        this.mEditConnectUrl = findViewById(R.id.editConnectUrl);
    }

    private void launchActivity(EventHandler eventHandler) {
        String url = getEditConnectUrl();

        if(url.length() > 0) {
            // Null out text so we can repeat with new link after Connect Activity closes.
            mEditConnectUrl.setText("");

            Log.i(TAG, ">>> Launching Connect activity");

            Connect.start(this, url, getString(R.string.deepLinkUrl).concat("://"), eventHandler);
        }
    }

    private String getEditConnectUrl() {
        String rawUrl = this.mEditConnectUrl.getText().toString();
        return rawUrl.replace("localhost:", "10.0.2.2:");
    }
}
