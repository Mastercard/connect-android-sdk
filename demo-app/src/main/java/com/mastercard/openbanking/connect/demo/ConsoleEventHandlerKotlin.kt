package com.mastercard.openbanking.connect.demo

import android.util.Log
import com.mastercard.openbanking.connect.EventHandler
import org.json.JSONObject

class ConsoleEventHandlerKotlin : EventHandler {
    companion object {
        private const val TAG = "ConsoleEventHandlerKt"
    }

    override fun onLoad() {
        Log.i(TAG, ">>> ConsoleEventHandlerKt: Received Loaded event")
    }

    override fun onDone(doneEvent: JSONObject) {
        Log.i(TAG, ">>> ConsoleEventHandlerKt: Received Done event\n>>>>>> ${doneEvent.toString()}")
    }

    override fun onCancel(cancelEvent: JSONObject) {
        Log.i(TAG, ">>> ConsoleEventHandlerKt: Received Cancel event\n>>>>>> ${cancelEvent.toString()}")
    }

    override fun onError(errorEvent: JSONObject) {
        Log.i(TAG, ">>> ConsoleEventHandlerKt: Received Error event\n>>>>>> ${errorEvent.toString()}")
    }

    override fun onRoute(routeEvent: JSONObject) {
        Log.i(TAG, ">>> ConsoleEventHandlerKt: Received Route event\n>>>>>> ${routeEvent.toString()}")
    }

    override fun onUser(userEvent: JSONObject) {
        Log.i(TAG, ">>> ConsoleEventHandlerKt: Received User event\n>>>>>> ${userEvent.toString()}")
    }
}

