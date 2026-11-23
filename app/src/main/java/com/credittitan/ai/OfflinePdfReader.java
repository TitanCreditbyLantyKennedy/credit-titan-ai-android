package com.credittitan.ai;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

/** OCR a private PDF locally. The returned text is unverified and must be reviewed. */
public final class OfflinePdfReader {
    public static final int MAX_PAGES = 60;
    public static final int MAX_TEXT_CHARS = LocalReports.MAX_TEXT_CHARS;
    private static final int MAX_RENDER_DIMENSION = 1800;

    private OfflinePdfReader() {}

    public static String extract(File pdf) throws IOException {
        if (pdf == null || !pdf.isFile() || !pdf.canRead()) {
            throw new IOException("The saved PDF is not available for reading.");
        }

        StringBuilder text = new StringBuilder();
        try (ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(descriptor);
             TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)) {
            if (renderer.getPageCount() < 1) throw new IOException("This PDF has no readable pages.");
            int pagesToRead = Math.min(renderer.getPageCount(), MAX_PAGES);
            for (int index = 0; index < pagesToRead; index++) {
                try (PdfRenderer.Page page = renderer.openPage(index)) {
                    int sourceWidth = page.getWidth();
                    int sourceHeight = page.getHeight();
                    if (sourceWidth < 1 || sourceHeight < 1) continue;
                    float scale = Math.min(2f, MAX_RENDER_DIMENSION / (float) Math.max(sourceWidth, sourceHeight));
                    int width = Math.max(1, Math.round(sourceWidth * scale));
                    int height = Math.max(1, Math.round(sourceHeight * scale));
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    try {
                        new Canvas(bitmap).drawColor(Color.WHITE);
                        Matrix transform = new Matrix();
                        transform.setScale(width / (float) sourceWidth, height / (float) sourceHeight);
                        page.render(bitmap, null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        Text result;
                        try {
                            result = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)));
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Report reading was interrupted. Try again.", interrupted);
                        } catch (Exception failure) {
                            throw new IOException("The text reader could not process page " + (index + 1) + ".", failure);
                        }
                        text.append("--- Page ").append(index + 1).append(" ---\n");
                        text.append(result.getText()).append("\n\n");
                        if (text.length() > MAX_TEXT_CHARS) {
                            throw new IOException("This report contains more text than the local review can hold. No extracted text was saved.");
                        }
                    } finally {
                        bitmap.recycle();
                    }
                }
            }
            if (renderer.getPageCount() > MAX_PAGES) {
                text.append("\n[Only the first ").append(MAX_PAGES).append(" pages were read. The remaining pages were not processed.]\n");
            }
        } catch (IOException expected) {
            throw expected;
        } catch (RuntimeException failure) {
            throw new IOException("This PDF could not be opened. It may be damaged, encrypted, or unsupported.", failure);
        }

        String extracted = text.toString().trim();
        if (extracted.isEmpty() || extracted.replaceAll("(?m)^--- Page \\d+ ---$", "").trim().isEmpty()) {
            throw new IOException("No readable text was found. This report may be image-only or too faint to recognize.");
        }
        return extracted;
    }
}
