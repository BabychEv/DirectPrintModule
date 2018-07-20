package com.test.webport.directprint.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.test.webport.directprint.module.DirectPrintModule;

public class PrintModuleReceiver extends BroadcastReceiver {
    public static final String PRINT_RESULT_ACTION = "print_module_receiver_result";
    private DirectPrintModule module;

    public PrintModuleReceiver(DirectPrintModule module) {
        this.module = module;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(PRINT_RESULT_ACTION))
                module.sendFeedBack(intent.getStringExtra(PRINT_RESULT_ACTION));
        }
    }
}
