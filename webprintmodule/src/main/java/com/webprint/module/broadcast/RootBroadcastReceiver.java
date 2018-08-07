package com.webprint.module.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webprint.module.PrintActivity;
import com.webprint.module.activity.BluetoothActivity;

public class RootBroadcastReceiver extends BroadcastReceiver {
    public static final String CLOSE_ACTIVITY_ACTION = "com.webprint.module.broadcast.RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION";
    public static final String PRINT_TEXT_ACTION = "com.webprint.module.broadcast.RootBroadcastReceiver.PRINT_TEXT_ACTION";
    public static final String EXTRA_TEXT_PRINT = "com.webprint.module.broadcast.RootBroadcastReceiver.EXTRA_TEXT_PRINT";
    private BluetoothActivity activity;

    public RootBroadcastReceiver(BluetoothActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(CLOSE_ACTIVITY_ACTION)) {
                activity.finishAndRemoveTask();
            } else if (intent.getAction().equals(PRINT_TEXT_ACTION)) {
                activity.print(intent.getStringExtra(EXTRA_TEXT_PRINT));
            }
        }
    }
}
