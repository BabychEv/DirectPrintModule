package com.webprint.module.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.print.sdk.PrinterConstants;
import com.android.print.sdk.PrinterInstance;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.webprint.module.R;
import com.webprint.module.broadcast.RootBroadcastReceiver;
import com.webprint.module.utils.PrintSharedPreferences;
import com.webprint.module.utils.Utils;

import java.util.Set;

public class PrintActivity extends FragmentActivity {

    public static final String BUNDLE_PRINT_TEXT = "com.webprint.module.activity.PrintActivity.BUNDLE_PRINT_TEXT";

    private TextView mTextViewStatus;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private String mAddress;
    private String mPrintText;
    private PrinterInstance mPrinter;
    private boolean mPrinterConnected;
    private Dialog mDialog;
    private RootBroadcastReceiver mCloseReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        mTextViewStatus = findViewById(R.id.text_view_status);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(PrintActivity.this,
                    R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mAddress = PrintSharedPreferences.getAddress(this, null);
        if (TextUtils.isEmpty(mAddress)) {
            Toast.makeText(PrintActivity.this, R.string.no_devices_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (getIntent() != null && getIntent().hasExtra(BUNDLE_PRINT_TEXT)) {
            mPrintText = getIntent().getStringExtra(BUNDLE_PRINT_TEXT);
            if (TextUtils.isEmpty(mPrintText)) {
                Toast.makeText(PrintActivity.this, R.string.not_printed_data, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(PrintActivity.this, R.string.not_printed_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mDialog = Utils.getProgressDialog(this);
        mCloseReceiver = new RootBroadcastReceiver(this);
        loadPairedDevice();
    }

    @SuppressLint("CheckResult")
    private void loadPairedDevice() {
        new RxPermissions(PrintActivity.this)
                .request(Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Toast.makeText(PrintActivity.this, R.string.bluetooth_not_ready, Toast.LENGTH_SHORT).show();
                        }
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            BluetoothDevice[] devices = (BluetoothDevice[]) pairedDevices.toArray();
                            for (BluetoothDevice device : devices) {
                                if (mAddress.equals(device.getAddress())) {
                                    mDevice = device;
                                    openPrinter();
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(PrintActivity.this, R.string.no_devices_found, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                    }
                }, error -> {
                    hideProgressBar();
                });
    }

    private void print(String text) {
        if (mPrinter != null && mPrinterConnected) {
            mPrinter.printText(text);
            mTextViewStatus.setText(R.string.printing);
            hideProgressBar();
        } else {
            Toast.makeText(PrintActivity.this, R.string.printer_not_ready, Toast.LENGTH_SHORT).show();
        }
    }

    private void openPrinter() {
        showProgressBar();
        mPrinter = new PrinterInstance(this, mDevice, mPrinterHandler);
        // default is gbk...
        // mPrinter.setEncoding("gbk");
        mPrinter.openConnection();
    }

    @SuppressLint("HandlerLeak")
    private Handler mPrinterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS:
                    mPrinterConnected = true;
                    mTextViewStatus.setText(R.string.printer_connection_success);
                    print(mPrintText);
                    break;
                case PrinterConstants.Connect.FAILED:
                    mPrinterConnected = false;
                    Toast.makeText(PrintActivity.this, R.string.printer_connection_failed, Toast.LENGTH_SHORT).show();
                    mTextViewStatus.setText(R.string.printer_connection_failed);
                    break;
                case PrinterConstants.Connect.CLOSED:
                    mPrinterConnected = false;
                    Toast.makeText(PrintActivity.this, R.string.printer_connection_close, Toast.LENGTH_SHORT).show();
                    mTextViewStatus.setText(R.string.printer_connection_close);
                    break;
                default:
                    break;
            }
        }
    };

    private void disconnectPrinter() {
        if (mPrinter != null) {
            mPrinter.closeConnection();
        }
    }

    public void showProgressBar() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void hideProgressBar() {
        if (mDialog != null) {
            mDialog.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        disconnectPrinter();
        unregisterReceiver(mCloseReceiver);
        super.onDestroy();
    }
}
