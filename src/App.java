import java.nio.file.*;
import java.io.*;
import java.util.*;

public class App {
    /**
     * Entry point: reads data/input.csv, runs the O(nm) pipeline, writes outputs under out/.
     *
     * TODO: after you implement CsvIO and Algo, this main should:
     *  1) CsvIO.read(input)
     *  2) Algo.normalizeByFirstRow(data)
     *  3) Algo.dropAllZeroColumns(data)
     *  4) int[] order = Algo.radixOrder(data); Algo.applyColumnOrder(data, order)
     *  5) Algo.BuildResult res = Algo.buildUnrootedPP(data)
     *  6) CsvIO.writeSplits / writeWitness, and write tree_unrooted.nwk via res.tree
     */
    public static void main(String[] args) {
        try {
            Path dataDir = Paths.get("data");
            Path outDir  = Paths.get("out");
            Path input   = dataDir.resolve("input.csv");
            Files.createDirectories(outDir);

            // TODO: uncomment once implemented
            // CsvIO.Data data = CsvIO.read(input);
            // Algo.normalizeByFirstRow(data);
            // Algo.dropAllZeroColumns(data);
            // int[] order = Algo.radixOrder(data);
            // Algo.applyColumnOrder(data, order);
            // Algo.BuildResult res = Algo.buildUnrootedPP(data);
            // CsvIO.writeSplits(outDir.resolve("splits.csv"), res.sortedChars, res.splitTaxa);
            // CsvIO.writeWitness(outDir.resolve("witness.txt"), res.witnessMessage);
            // Files.writeString(outDir.resolve("tree_unrooted.nwk"), res.tree.toNewickUnrooted() + "\n");

            // Temporary stub so this file compiles and runs before you implement the above:
            Files.writeString(outDir.resolve("witness.txt"), "TODO: implement pipeline in App/main\n");
            Files.writeString(outDir.resolve("tree_unrooted.nwk"), "();\n");
            Files.writeString(outDir.resolve("splits.csv"), "character,clade\n");
            System.out.println("Skeleton run complete. Implement TODOs to enable full pipeline.");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
