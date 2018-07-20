package com.test.webport.directprint.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.test.webport.directprint.PrintDocumentAdapterWrapper;
import com.test.webport.directprint.broadcast.PrintModuleReceiver;
import com.test.webport.directprint.broadcast.RootBroadcastReceiver;
import com.test.webport.directprint.module.DirectPrintModule;

public class PrintModuleActivity extends AppCompatActivity {
    private RootBroadcastReceiver receiver;
    private ProgressDialog dialog;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        webView = new WebView(this);
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webView.loadDataWithBaseURL("",
                getIntent().getStringExtra(DirectPrintModule.BILL_TEXT_KEY),
                "text/html",
                "UTF-8",
                null);
        addWebViewClient();
        initReceiver();
    }

    private void addWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {

            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                dialog = ProgressDialog.show(PrintModuleActivity.this, "Please Wait...",
                        "Loading...");
                dialog.setCancelable(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Intent result = new Intent(PrintModuleReceiver.PRINT_RESULT_ACTION);
                result.putExtra(PrintModuleReceiver.PRINT_RESULT_ACTION,
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? error.getDescription() : "Html syntax error");
                sendBroadcast(result);
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                doPrint();
            }
        });
    }

    private void doPrint() {
        if (!isFinishing()) {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String jobName = "Start print document - " + System.currentTimeMillis();
            PrintDocumentAdapterWrapper printAdapter = new PrintDocumentAdapterWrapper(this, webView.createPrintDocumentAdapter(jobName));
            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build();
            if (printManager != null)
                printManager.print(jobName, printAdapter, attributes);
        }
    }

    private void initReceiver() {
        receiver = new RootBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override protected void onDestroy() {
        dialog.dismiss();
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
