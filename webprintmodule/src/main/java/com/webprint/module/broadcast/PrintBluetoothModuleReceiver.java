package com.webprint.module.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webprint.module.module.WebPrintModule;

public class PrintBluetoothModuleReceiver extends BroadcastReceiver {

    public static final String PRINT_RESULT_ACTION = "com.webprint.module.broadcast.PrintBluetoothModuleReceiver.PRINT_RESULT_ACTION";
    private WebPrintModule module;

    public PrintBluetoothModuleReceiver(WebPrintModule module) {
        this.module = module;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(PRINT_RESULT_ACTION))
                module.sendFeedBack(intent.getStringExtra(PRINT_RESULT_ACTION));
        }
    }
}