package com.mastercard.openbanking.connect.demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.mastercard.openbanking.connect.Connect
import com.mastercard.openbanking.connect.EventHandler

class MainActivityKotlin : AppCompatActivity() {
    companion object {
        private const val TAG = "ConnectKotlin"
    }

    private lateinit var editConnectUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_kotlin)

        val startButtonEventHandler = findViewById<Button>(R.id.startButtonEventHandler)

        startButtonEventHandler.setOnClickListener {
            launchActivity(ConsoleEventHandlerKotlin())
        }

        editConnectUrl = findViewById(R.id.editConnectUrl)

        val switchToJavaButton = findViewById<Button>(R.id.switchToJavaButton)
        switchToJavaButton.setOnClickListener {
            val intent = Intent(this, MainActivityJava::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun launchActivity(eventHandler: EventHandler) {
        val url = getEditConnectUrl()
        if (url.isNotEmpty()) {
            // Null out text so we can repeat with new link after Connect Activity closes.
            editConnectUrl.setText("")
            Log.i(TAG, ">>> Launching Connect activity")
            Connect.start(this, url, eventHandler)
        }
    }

    private fun getEditConnectUrl(): String {
        val rawUrl = editConnectUrl.text.toString()
        return rawUrl.replace("localhost:", "10.0.2.2:")
    }
}