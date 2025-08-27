import java.nio.file.*;
import java.io.*;
import java.util.*;

public class CsvIO {

    /** Container for the matrix and names. */
    public static class Data {
        public String[] taxa;   // length n
        public String[] chars;  // length m
        public int[][]  C;      // n x m (0/1)
        public int n, m;
        public Data(String[] taxa, String[] chars, int[][] C) {
            this.taxa = taxa; this.chars = chars; this.C = C;
            this.n = taxa.length; this.m = chars.length;
        }
    }

    /**
     * Read CSV: header must have first column named 'taxon'.
     * Validate 0/1 entries. No quoted commas support (keep it simple).
     *
     * TODO: implement real parsing.
     */
    public static Data read(Path csvPath) throws IOException {
        // TODO: parse lines, build taxa[], chars[], C[][]
        // Stub to compile:
        String[] taxa = new String[0];
        String[] chars = new String[0];
        int[][] C = new int[0][0];
        return new Data(taxa, chars, C);
    }

    /** Write splits.csv with two columns: character,clade (space-separated taxa). */
    public static void writeSplits(Path outCsv, String[] sortedChars, List<String>[] splitTaxa) throws IOException {
        // TODO: open writer and dump rows. Each row: charName + "," + joinedTaxa
        try (BufferedWriter bw = Files.newBufferedWriter(outCsv)) {
            bw.write("character,clade\n");
            if (sortedChars != null && splitTaxa != null) {
                for (int i = 0; i < sortedChars.length; i++) {
                    String name = sortedChars[i] == null ? "" : sortedChars[i];
                    List<String> lst = (splitTaxa[i] == null) ? Collections.emptyList() : splitTaxa[i];
                    String clade = String.join(" ", lst);
                    bw.write(name + "," + clade + "\n");
                }
            }
        }
    }

    /** Write witness.txt with a single short line ("OK" or a conflict message). */
    public static void writeWitness(Path outTxt, String msg) throws IOException {
        // TODO: you may add more structured info if you like
        Files.writeString(outTxt, (msg == null ? "" : msg) + "\n");
    }

    // --- helper (optional) ---
    static String[] splitCsv(String line) {
        // TODO: if you want to support quotes, expand this. For now: simple split on commas.
        return line.split("\\s*,\\s*");
    }
}
