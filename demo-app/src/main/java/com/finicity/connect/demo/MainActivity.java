package com.finicity.connect.demo;



import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.finicity.connect.sdk.Connect;

public class MainActivity extends AppCompatActivity {

    private Button mStartButton;
    private EditText mEditConnectUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add click listener to start button which starts Connect activity
        this.mStartButton = findViewById(R.id.startButton);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchConnectActivity();
            }
        });

        this.mEditConnectUrl = findViewById(R.id.editConnectUrl);
    }

    private void launchConnectActivity() {
        String url = getEditConnectUrl();

        if(url.length() > 0) {
            // Null out text so we can repeat with new link after Connect Activity closes.
            mEditConnectUrl.setText("");

            System.out.println(">>> Launching Connect activity");
            Connect.start(this, url, new ConsoleEventListener());
        }
    }

    private String getEditConnectUrl() {
        String rawUrl = this.mEditConnectUrl.getText().toString();

        return rawUrl.replace("localhost:", "10.0.2.2:");
    }
}
