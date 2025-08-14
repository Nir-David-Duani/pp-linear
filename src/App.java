import java.nio.file.*;
import java.io.*;
import java.util.*;

public class App {
    /**
     * Entry point: reads data/input.csv, runs the O(nm) pipeline, writes outputs under out/.
     */
    public static void main(String[] args) {
        try {
            Path dataDir = Paths.get("data");
            Path outDir  = Paths.get("out");
            Path input   = dataDir.resolve("input.csv");
            Files.createDirectories(outDir);

            // 1) Read CSV → Data
            CsvIO.Data data = CsvIO.read(input);

            // 2) Normalize by reference taxon (first taxon row)
            Algo.normalizeByFirstRow(data);

            // 3) Drop all-zero columns
            Algo.dropAllZeroColumns(data);

            // 4) Radix sort columns (stable, bottom-up) → produces a column order
            int[] order = Algo.radixOrder(data);
            Algo.applyColumnOrder(data, order);

            // 5) Build unrooted PP (partition refinement)
            Algo.BuildResult res = Algo.buildUnrootedPP(data);

            // 6) Write outputs
            CsvIO.writeSplits(outDir.resolve("splits.csv"), res.sortedChars, res.splitTaxa);
            CsvIO.writeWitness(outDir.resolve("witness.txt"), res.witnessMessage);
            Files.writeString(outDir.resolve("tree_unrooted.nwk"), res.tree.toNewickUnrooted() + "\n");

            // Optional short trace for debugging
            Files.writeString(outDir.resolve("trace.txt"),
                    "n=" + data.n + " m=" + data.m +
                    " | perfect=" + res.isPerfect +
                    " | columnsSorted=" + Arrays.toString(res.sortedChars) + "\n");

            System.out.println("Done. See out/ for outputs.");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
