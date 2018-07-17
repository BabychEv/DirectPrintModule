package com.test.webport.directprint.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.test.webport.directprint.BuildConfig;
import com.test.webport.directprint.broadcast.PrintModuleReceiver;
import com.test.webport.directprint.broadcast.RootBroadcastReceiver;
import com.test.webport.directprint.module.DirectPrintModule;

public class PrintModuleActivity extends AppCompatActivity {
    private RootBroadcastReceiver receiver;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webView.loadDataWithBaseURL("",
                getIntent().getStringExtra(DirectPrintModule.BILL_TEXT_KEY),
                "text/html",
                "UTF-8",
                null);
        webView.setWebViewClient(new WebViewClient(){

            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Intent result = new Intent(PrintModuleReceiver.PRINT_RESULT_ACTION);
                result.putExtra(PrintModuleReceiver.PRINT_RESULT_ACTION,
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? error.getDescription() : "Html syntax error");
                sendBroadcast(result);
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                doPrint();
            }
        });
        initReceiver();
    }

    private void initReceiver() {
        receiver = new RootBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    private void doPrint() {
        String jobName = "Start print" + BuildConfig.VERSION_NAME + "document";
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        PrintAttributes attributes = new PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
        if (printManager != null)
            printManager.print(jobName, printAdapter, attributes);
    }

    @Override protected void onDestroy() {
        sendBroadcast(new Intent(PrintModuleReceiver.CLOSE_ACTION));
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
