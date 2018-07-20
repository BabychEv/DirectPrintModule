package com.test.webport.directprint.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.test.webport.directprint.activity.PrintModuleActivity;

public class RootBroadcastReceiver extends BroadcastReceiver {
    public static final String CLOSE_ACTIVITY_ACTION = "close_print_activity_action";
    private PrintModuleActivity activity;

    public RootBroadcastReceiver(PrintModuleActivity activity) {
        this.activity = activity;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(CLOSE_ACTIVITY_ACTION)) {
                activity.finishAndRemoveTask();
            }
        }
    }
}
