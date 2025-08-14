import java.util.*;

public class Algo {

    // -------- Stage 1: Normalization --------

    /** Flip each column j where C[0][j] == 1 so the top row becomes all zeros. */
    public static void normalizeByFirstRow(CsvIO.Data data) {
        // TODO: implement in-place flip (0<->1) per column where first taxon has 1
    }

    /** Remove all-zero columns; compact data.chars and data.C; update data.m. */
    public static void dropAllZeroColumns(CsvIO.Data data) {
        // TODO: scan columns, keep only those with at least one 1
    }

    // -------- Stage 2: Radix sort (stable, bottom-up) --------

    /**
     * Compute a stable lexicographic order of columns by performing a pass for each row
     * from bottom to top (binary counting-sort per pass). Return array of column indices.
     */
    public static int[] radixOrder(CsvIO.Data data) {
        // TODO: implement binary stable partition on each pass
        // Stub:
        int[] order = new int[data.m];
        for (int j = 0; j < data.m; j++) order[j] = j;
        return order;
    }

    /** Apply the given column order to data.C and data.chars (in-place repack). */
    public static void applyColumnOrder(CsvIO.Data data, int[] order) {
        // TODO: rebuild arrays in the new order
    }

    // -------- Stage 3: Partition refinement --------

    public static class BuildResult {
        public final boolean isPerfect;
        public final String witnessMessage; // "OK" if perfect; short conflict otherwise
        public final String[] sortedChars;
        public final List<String>[] splitTaxa; // per character: taxa with 1
        public final Tree tree;
        public BuildResult(boolean ok, String msg, String[] sortedChars, List<String>[] splitTaxa, Tree tree) {
            this.isPerfect = ok; this.witnessMessage = msg; this.sortedChars = sortedChars; this.splitTaxa = splitTaxa; this.tree = tree;
        }
    }

    /**
     * Build unrooted PP via iterative refinement over clades.
     * Steps per character j (after sorting):
     *  - Oj = taxa with 1 in column j
     *  - find containing clade (conflict if spans >1)
     *  - if Oj equals the clade ⇒ co-label; else split and connect with edge label j
     *  - after all columns, attach leaves and contract deg-2 nodes
     */
    public static BuildResult buildUnrootedPP(CsvIO.Data data) {
        // TODO: implement full refinement
        // Minimal stub to compile and allow end-to-end wiring in App:
        Tree t = new Tree();
        // you may want to do: int root = t.makeInternal(); ...
        @SuppressWarnings("unchecked")
        List<String>[] splits = new List[data.m];
        String[] names = data.chars;
        boolean ok = false; // change to true when implemented
        String msg = "TODO: buildUnrootedPP not yet implemented";
        return new BuildResult(ok, msg, names == null ? new String[0] : names, splits, t);
    }

    // ---- helper suggestions (implement or adjust as you like) ----
    static int[] onesSet(int[][] C, int col) { /* TODO */ return new int[0]; }
    static int findContainingClade(List<int[]> clades, int[] cladeOfTaxon, int[] Oj) { /* TODO */ return -1; }
    static boolean equalsSet(int[] A, int[] B) { /* TODO */ return false; }
    static int splitClade(List<int[]> clades, int[] cladeOfTaxon, int cl, int[] Oj) { /* TODO */ return -1; }
    static String minimalConflictMsg(String cj, int[] Oj, List<String> prevChars, List<int[]> prevO, String[] taxaNames) { /* TODO */ return "Conflict: TODO"; }
}

