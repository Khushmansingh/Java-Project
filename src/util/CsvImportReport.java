package util;

import java.util.ArrayList;
import java.util.List;

public class CsvImportReport {
    private int imported;
    private int skipped;
    private final List<String> rejectedRows = new ArrayList<>();
    public void imported() { imported++; }
    public void skipped(int row) { skipped++; rejectedRows.add("Row " + row + ": skipped (blank row)"); }
    public void rejected(int row, String reason) { rejectedRows.add("Row " + row + ": " + reason); }
    public int getImported() { return imported; }
    public int getRejected() { return rejectedRows.size() - skipped; }
    public int getSkipped() { return skipped; }
    public List<String> getDetails() { return new ArrayList<>(rejectedRows); }
}
