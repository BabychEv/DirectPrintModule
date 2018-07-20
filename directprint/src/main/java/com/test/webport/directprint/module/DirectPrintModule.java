package com.test.webport.directprint.module;

import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.test.webport.directprint.activity.PrintModuleActivity;
import com.test.webport.directprint.broadcast.PrintModuleReceiver;
import com.test.webport.directprint.broadcast.RootBroadcastReceiver;

public class DirectPrintModule extends ReactContextBaseJavaModule {
    public static final String BILL_TEXT_KEY = "bill_text_key";
    private static final String PRINT_RESULT_KEY = "print_result_key";
    private static final String PRINT_RESULT_EVENT = "print_result_event";
    private final PrintModuleReceiver receiver;

    public DirectPrintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        receiver = new PrintModuleReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PrintModuleReceiver.PRINT_RESULT_ACTION);
        getReactApplicationContext().registerReceiver(receiver, filter);
    }

    @Override public String getName() {
        return "DirectPrint";
    }

    @ReactMethod
    public void startPrint(String htmlText){
        Intent intent = new Intent(getReactApplicationContext(), PrintModuleActivity.class);
        intent.putExtra(BILL_TEXT_KEY, htmlText);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void onBackPressed() {
        getReactApplicationContext().sendBroadcast(new Intent(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION));
    }

    public void sendFeedBack(String feedback) {
        WritableMap params = Arguments.createMap();
        params.putString(PRINT_RESULT_KEY, feedback);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(PRINT_RESULT_EVENT, params);
    }
}
