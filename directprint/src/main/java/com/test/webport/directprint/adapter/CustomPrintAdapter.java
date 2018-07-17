package com.test.webport.directprint.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;

import com.test.webport.directprint.activity.PrintModuleActivity;
import com.test.webport.directprint.broadcast.PrintModuleReceiver;

import java.io.FileOutputStream;
import java.io.IOException;

public class CustomPrintAdapter extends PrintDocumentAdapter {
    private final PrintModuleActivity activity;
    private View view;
    private PrintedPdfDocument pdfDocument;

    public CustomPrintAdapter(PrintModuleActivity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    @Override public void onStart() {
        super.onStart();
    }

    @Override public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        pdfDocument = new PrintedPdfDocument(activity, newAttributes);
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }
        PrintDocumentInfo info = new PrintDocumentInfo
                .Builder("print_output_" + System.currentTimeMillis() + ".pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(3)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        // Start the page
        PdfDocument.Page page = pdfDocument.startPage(0);
        // Create a bitmap and put it a canvas for the view to draw to. Make it the size of the view
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        // create a Rect with the view's dimensions.
        Rect src = new Rect(0, 0, view.getWidth(), view.getHeight());
        // get the page canvas and measure it.
        Canvas pageCanvas = page.getCanvas();
        float pageWidth = pageCanvas.getWidth();
        float pageHeight = pageCanvas.getHeight();
        // how can we fit the Rect src onto this page while maintaining aspect ratio?
        float scale = Math.min(pageWidth/src.width(), pageHeight/src.height());
        float left = pageWidth / 2 - src.width() * scale / 2;
        float top = pageHeight / 2 - src.height() * scale / 2;
        float right = pageWidth / 2 + src.width() * scale / 2;
        float bottom = pageHeight / 2 + src.height() * scale / 2;
        RectF dst = new RectF(left, top, right, bottom);
        pageCanvas.drawBitmap(bitmap, src, dst, null);
        pdfDocument.finishPage(page);
        try {
            pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            Intent result = new Intent(PrintModuleReceiver.PRINT_RESULT_ACTION);
            result.putExtra(PrintModuleReceiver.PRINT_RESULT_ACTION, "file ");
            activity.sendBroadcast(result);
            return;
        } finally {
            pdfDocument.close();
            pdfDocument = null;
        }
        callback.onWriteFinished(new PageRange[]{new PageRange(0, 0)});
    }

    @Override public void onFinish() {
        super.onFinish();
        activity.finish();
    }
}
