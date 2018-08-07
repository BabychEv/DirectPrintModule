package com.webprint.module.activity;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.webprint.module.BluetoothDeviceList;
import com.webprint.module.R;
import com.webprint.module.utils.Utils;

import java.util.Set;

public class BluetoothActivity extends FragmentActivity {

    //UI
    private Button mScanButton;
    private RecyclerView mDevicesRecyclerView;
    private BluetoothDevicesAdapter mBluetoothDevicesAdapter;
    private Dialog mDialog;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_print);
        setActionBar(null);
        initView();
        loadPairedDevices();
    }

    private void initView() {
        mDialog = Utils.getProgressDialog(this);
        mScanButton = findViewById(R.id.btn_scan);
        mDevicesRecyclerView = findViewById(R.id.recycler_view);
        mScanButton.setOnClickListener(click -> {
            new RxPermissions(BluetoothActivity.this)
                    .request(Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
                            if(!mBluetoothAdapter.isEnabled()) {
                                mBluetoothAdapter.enable();
                            }
                            scanDevices();
                        } else {
                        }
                    }, error -> {

                    });
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(BluetoothActivity.this,
                    R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mBluetoothDevicesAdapter = new BluetoothDevicesAdapter();
        mDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDevicesRecyclerView.setAdapter(mBluetoothDevicesAdapter);
    }

    private void loadPairedDevices() {
        new RxPermissions(BluetoothActivity.this)
                .request(Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        if(!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                        }
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                mBluetoothDevicesAdapter.addItems(device);
                            }
                        }
                    } else {
                    }
                }, error -> {

                });

    }

    private void scanDevices() {
        showProgressBar();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothDevicesAdapter.clearData();
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBluetoothReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            hideProgressBar();
        }
        this.unregisterReceiver(mBluetoothReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBluetoothDevicesAdapter.addItems(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                hideProgressBar();
                if (mBluetoothDevicesAdapter.getItemCount() == 0) {
                    Toast.makeText(BluetoothActivity.this,
                            R.string.no_devices_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}
