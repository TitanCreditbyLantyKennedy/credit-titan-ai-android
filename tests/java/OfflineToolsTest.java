import com.credittitan.ai.CreditTools;
import com.credittitan.ai.LocalReports;
import java.io.*;
import java.nio.file.*;

/** Executable without Android, exercises file limits and real calculation/draft behavior. */
public class OfflineToolsTest {
    private static int checks;
    private static void check(boolean condition) { checks++; if (!condition) throw new AssertionError("Check " + checks); }
    interface Operation { void run() throws Exception; }
    private static void rejects(Operation operation) throws Exception {
        boolean rejected = false;
        try { operation.run(); } catch (IllegalArgumentException | IOException expected) { rejected = true; }
        check(rejected);
    }
    public static void main(String[] args) throws Exception {
        check(CreditTools.utilization("320", "1000").equals("32.0%"));
        check(CreditTools.utilization("0", "1000").equals("0.0%"));
        check(CreditTools.utilization("1200", "1000").equals("120.0%"));
        check(CreditTools.utilization("1", "3").equals("33.3%"));
        for (String invalid : new String[]{"", "NaN", "-1", "1.001", "1e12", "9999999999999"}) rejects(() -> CreditTools.utilization(invalid, "1000"));
        rejects(() -> CreditTools.utilization("1", "0"));
        rejects(() -> CreditTools.utilization("1", "-1"));
        for (String bureau : CreditTools.BUREAUS) {
            String draft = CreditTools.draft(bureau, "  The reported balance differs from my statement.  ");
            check(draft.contains("To: " + bureau)); check(draft.contains("The reported balance differs"));
            check(draft.contains("has not been sent")); check(!draft.contains("guarantee"));
        }
        rejects(() -> CreditTools.draft("Unknown", "An observation"));
        rejects(() -> CreditTools.draft("Experian", "  "));
        Path temp = Files.createTempDirectory("titan-report-tests");
        try {
            LocalReports reports = new LocalReports(temp.toFile());
            check(reports.list().length == 0);
            rejects(() -> reports.save(new ByteArrayInputStream("not a PDF".getBytes())));
            rejects(() -> reports.save(new ByteArrayInputStream("%PD".getBytes())));
            check(reports.list().length == 0);
            byte[] pdf = "%PDF-1.7\nTest fixture only\n%%EOF".getBytes();
            File saved = reports.save(new ByteArrayInputStream(pdf));
            check(java.util.Arrays.equals(Files.readAllBytes(saved.toPath()), pdf));
            check(new LocalReports(temp.toFile()).list().length == 1);
            rejects(() -> reports.delete(temp.resolve("outside.pdf").toFile()));
            check(reports.delete(saved));
            InputStream broken = new InputStream() { int i; public int read() throws IOException {
                if (i < 5) return "%PDF-".charAt(i++); throw new IOException("Interrupted provider");
            }};
            rejects(() -> reports.save(broken));
            check(reports.list().length == 0);
            InputStream oversized = new InputStream() { long i; public int read() {
                if (i < 5) return "%PDF-".charAt((int)i++);
                return i++ <= LocalReports.MAX_BYTES ? 32 : -1;
            }};
            rejects(() -> reports.save(oversized));
            check(reports.list().length == 0);
            check(temp.resolve("reports").toFile().list().length == 0);
            for (int i = 0; i < 9; i++) reports.save(new ByteArrayInputStream(pdf));
            rejects(() -> reports.save(new ByteArrayInputStream(pdf)));
            check(reports.list().length == 9);
            Files.write(temp.resolve("reports/interrupted.pending"), pdf);
            reports.clear();
            check(reports.list().length == 0);
            check(temp.resolve("reports").toFile().list().length == 0);
        } finally {
            try (java.util.stream.Stream<Path> files = Files.walk(temp)) {
                files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.delete(p); } catch (IOException e) { throw new RuntimeException(e); } });
            }
        }
        System.out.println("PASS: " + checks + " offline tool and file-storage assertions");
    }
}
