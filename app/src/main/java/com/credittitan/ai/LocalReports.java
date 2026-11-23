package com.credittitan.ai;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/** Private report files and on-device OCR text. Nothing is uploaded. */
public final class LocalReports {
    public static final long MAX_BYTES = 20L * 1024 * 1024;
    private final File directory;
    public LocalReports(File files) { directory = new File(files, "reports"); }

    public synchronized File[] list() {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".pdf"));
        if (files == null) return new File[0];
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return files;
    }
    public synchronized File save(InputStream input) throws IOException {
        if (list().length >= 9) throw new IOException("You can keep up to nine reports. Remove a local copy before adding another.");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Unable to create private storage.");
        File pending = new File(directory, UUID.randomUUID() + ".pending");
        File destination = new File(directory, pending.getName().replace(".pending", ".pdf"));
        boolean complete = false;
        try {
            try (OutputStream output = new FileOutputStream(pending)) {
                byte[] header = new byte[5];
                int offset = 0;
                while (offset < header.length) {
                    int n = input.read(header, offset, header.length - offset);
                    if (n < 0) throw new IOException("This file does not have a PDF header.");
                    offset += n;
                }
                if (!Arrays.equals(header, new byte[]{'%', 'P', 'D', 'F', '-'})) throw new IOException("Choose a PDF file.");
                output.write(header);
                long count = header.length;
                byte[] buffer = new byte[8192];
                int n;
                while ((n = input.read(buffer)) != -1) {
                    count += n;
                    if (count > MAX_BYTES) throw new IOException("PDFs must be 20 MB or smaller.");
                    output.write(buffer, 0, n);
                }
            }
            if (!pending.renameTo(destination)) throw new IOException("Unable to finish saving the local copy.");
            complete = true;
            return destination;
        } finally {
            if (!complete) pending.delete();
        }
    }
    public synchronized void clear() throws IOException {
        File[] all = directory.listFiles();
        if (all == null) return;
        for (File file : all) if (!file.delete()) throw new IOException("Unable to remove every stored file.");
    }
    public synchronized boolean delete(File file) throws IOException {
        requireReport(file);
        File ocr = companion(file, ".ocr.txt");
        File reviewed = companion(file, ".review.txt");
        if (ocr.exists() && !ocr.delete()) throw new IOException("Unable to remove extracted report text.");
        if (reviewed.exists() && !reviewed.delete()) throw new IOException("Unable to remove reviewed report text.");
        return file.delete();
    }

    public synchronized String readOcrText(File report) throws IOException {
        return readText(companion(requireReport(report), ".ocr.txt"));
    }

    public synchronized String readReviewedText(File report) throws IOException {
        return readText(companion(requireReport(report), ".review.txt"));
    }

    public synchronized void saveOcrText(File report, String text) throws IOException {
        writeText(companion(requireReport(report), ".ocr.txt"), text);
        // A new OCR pass supersedes manual corrections, so never pair old
        // reviewed text with a newer extraction.
        File reviewed = companion(report, ".review.txt");
        if (reviewed.exists() && !reviewed.delete()) throw new IOException("Unable to remove older reviewed text.");
    }

    public synchronized void saveReviewedText(File report, String text) throws IOException {
        writeText(companion(requireReport(report), ".review.txt"), text);
    }

    private File requireReport(File report) throws IOException {
        if (report == null || !report.getName().endsWith(".pdf") || !report.isFile()
                || !directory.getCanonicalFile().equals(report.getCanonicalFile().getParentFile())) {
            throw new IOException("Invalid local report.");
        }
        return report;
    }

    private File companion(File report, String suffix) throws IOException {
        requireReport(report);
        return new File(directory, report.getName() + suffix);
    }

    private String readText(File file) throws IOException {
        if (!file.isFile()) return null;
        long length = file.length();
        if (length > OfflinePdfReader.MAX_TEXT_CHARS * 4L) throw new IOException("The saved review text is too large to open.");
        try (InputStream input = new FileInputStream(file); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void writeText(File file, String text) throws IOException {
        if (text == null || text.length() > OfflinePdfReader.MAX_TEXT_CHARS) {
            throw new IOException("The report text is too large to save.");
        }
        File temporary = new File(directory, UUID.randomUUID() + ".pending");
        boolean complete = false;
        File backup = new File(directory, UUID.randomUUID() + ".backup");
        boolean backedUp = false;
        try {
            try (FileOutputStream output = new FileOutputStream(temporary)) {
                output.write(text.getBytes(StandardCharsets.UTF_8));
                output.getFD().sync();
            }
            if (file.exists()) {
                if (!file.renameTo(backup)) throw new IOException("Unable to replace the saved report text.");
                backedUp = true;
            }
            if (!temporary.renameTo(file)) {
                if (backedUp) backup.renameTo(file);
                throw new IOException("Unable to save local report text.");
            }
            complete = true;
        } finally {
            if (!complete) temporary.delete();
            if (complete && backedUp) backup.delete();
        }
    }
}
