package com.mastercard.openbanking.connect.demo;



import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mastercard.openbanking.connect.Connect;
import com.mastercard.openbanking.connect.EventHandler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText mEditConnectUrl;
    private EditText editRedirectURL;
    private RadioGroup radioGroup;
    String redirectUrl = "https://acmelending.net"; // Default the redirectUrl to Prod

    private EditText editPingDelay;

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

        radioGroup = findViewById(R.id.radioGroup);
        editRedirectURL = findViewById(R.id.editRedirectURL);
        editPingDelay = findViewById(R.id.editPingDelay);
        setupRadioButtonEventHandlers();

    }

    private void setupRadioButtonEventHandlers() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleRadioButtonSelection(checkedId);
            }
        });
    }
    private void handleRadioButtonSelection(int checkedId) {

        RadioButton radioButton = findViewById(checkedId);
        if (radioButton == null) return;

        if (checkedId == R.id.radioSTG) {
            redirectUrl = "https://acme.finicitystg.com";
        } else if (checkedId == R.id.radioProd) {
            redirectUrl = "https://acmelending.net";
        } else if (checkedId == R.id.radioRedirectURL) {
            redirectUrl = "";
        }

        editRedirectURL.setVisibility(checkedId == R.id.radioRedirectURL ? View.VISIBLE : View.GONE);
    }

    private void launchActivity(EventHandler eventHandler) {
        String url = getEditConnectUrl();
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioRedirectURL) {
            redirectUrl = editRedirectURL.getText().toString();
        }
        String pingDelay = editPingDelay.getText().toString();
        if(url.length() > 0) {
            // Null out text so we can repeat with new link after Connect Activity closes.
            mEditConnectUrl.setText("");
            editRedirectURL.setText("");
            Log.i(TAG, ">>> Launching Connect activity");
//            Connect.start(this, url,redirectUrl, eventHandler,pingDelay); // new sdk
            Connect.start(this, url,redirectUrl, eventHandler); //old sdk
        }
    }

    private String getEditConnectUrl() {
        String rawUrl = this.mEditConnectUrl.getText().toString();
        return rawUrl.replace("localhost:", "10.0.2.2:");
    }

}
