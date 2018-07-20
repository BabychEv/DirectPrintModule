package com.test.webport.directprint;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;

import com.test.webport.directprint.activity.PrintModuleActivity;

public class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {
    private PrintModuleActivity activity;
    private final PrintDocumentAdapter delegate;

    public PrintDocumentAdapterWrapper(PrintModuleActivity activity, PrintDocumentAdapter adapter){
        super();
        this.activity = activity;
        this.delegate = adapter;
    }

    @Override public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
    }

    @Override public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        delegate.onWrite(pages, destination, cancellationSignal, callback);
    }

    public void onFinish(){
        delegate.onFinish();
        activity.finish();
    }
}
