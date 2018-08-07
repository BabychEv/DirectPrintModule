package com.webprint.module.module;

import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.webprint.module.activity.BluetoothActivity;
import com.webprint.module.activity.PrintActivity;
import com.webprint.module.broadcast.PrintBluetoothModuleReceiver;
import com.webprint.module.broadcast.RootBroadcastReceiver;

public class WebPrintModule extends ReactContextBaseJavaModule {

    private static final String PRINT_RESULT_KEY = "com.webprint.module.module.WebPrintModule.PRINT_RESULT_KEY";
    private static final String PRINT_RESULT_EVENT = "com.webprint.module.module.WebPrintModule.PRINT_RESULT_EVENT";
    private final PrintBluetoothModuleReceiver receiver;

    public WebPrintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        receiver = new PrintBluetoothModuleReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PrintBluetoothModuleReceiver.PRINT_RESULT_ACTION);
        getReactApplicationContext().registerReceiver(receiver, filter);
    }

    @Override
    public String getName() {
        return "WebPrint";
    }

    /**
     * Starts activity when user can pair bluetooth device
     */
    @ReactMethod
    public void startBluetoothPairing() {
        Intent intent = new Intent(getReactApplicationContext(), BluetoothActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getReactApplicationContext().startActivity(intent);
    }

    /**
     * Closes current activity
     */
    @ReactMethod
    public void onBackPressed() {
        getReactApplicationContext().sendBroadcast(new Intent(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION));
    }

    /**
     * Open activity which try to connect to printer and print @text
     */
    @ReactMethod
    public void print(String text) {
        Intent intent = new Intent(getReactApplicationContext(), PrintActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(PrintActivity.BUNDLE_PRINT_TEXT, text);
        getReactApplicationContext().startActivity(intent);
    }

    public void sendFeedBack(String feedback) {
        WritableMap params = Arguments.createMap();
        params.putString(PRINT_RESULT_KEY, feedback);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(PRINT_RESULT_EVENT, params);
    }
}
