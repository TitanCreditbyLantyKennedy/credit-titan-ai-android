package com.credittitan.ai;

import java.io.*;
import java.util.Arrays;
import java.util.UUID;

/** Private local copies only; never uploads or claims to parse report contents. */
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
        if (!directory.getCanonicalFile().equals(file.getCanonicalFile().getParentFile())) throw new IOException("Invalid local report.");
        return file.delete();
    }
}
