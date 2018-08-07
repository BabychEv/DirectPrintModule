package com.webprint.module.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.webprint.module.R;

public class BluetoothActivity extends FragmentActivity {

    private Button mScanButton;
    private RecyclerView mDevicesRecyclerView;
    private ContentLoadingProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_print);
        setActionBar(null);
        initView();
    }

    private void initView() {
        mScanButton = findViewById(R.id.btn_scan);
        mDevicesRecyclerView = findViewById(R.id.recycler_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.show();
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.hide();
        }
    }
}
