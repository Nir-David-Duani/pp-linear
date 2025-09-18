import java.util.*;

// Main algorithm for perfect phylogeny
public final class Algo {

    // Exception for non-perfect phylogeny
    public static final class NotPerfectPhylogenyException extends Exception {
        public final List<String> witnessChars;
        public final SortResult sortResult;
        public final Map<String, Set<String>> splitsByChar;
        public NotPerfectPhylogenyException(String message, List<String> witnessChars, SortResult sortResult, Map<String, Set<String>> splitsByChar) {
            super(message);
            this.witnessChars = witnessChars;
            this.sortResult = sortResult;
            this.splitsByChar = splitsByChar;
        }
    }

    // Result of sorting columns
    public static final class SortResult {
        public final int[][] Csorted;
        public final String[] charsSorted;
        public final int[] columnOrder;
        public SortResult(int[][] Csorted, String[] charsSorted, int[] columnOrder) {
            this.Csorted = Csorted; this.charsSorted = charsSorted; this.columnOrder = columnOrder;
        }
    }

    // Result of building the tree and outputs
    public static final class BuildResult {
        public final List<Set<Integer>> nodesTaxa;
        public final List<int[]> edges;
        public final Map<Integer, List<String>> edgeLabels;
        public final Map<String, Set<String>> splitsByChar;
        public final SortResult sort;
        public final String witness;
        public BuildResult(List<Set<Integer>> nodesTaxa, List<int[]> edges, Map<Integer, List<String>> edgeLabels,
                           Map<String, Set<String>> splitsByChar, SortResult sort, String witness) {
            this.nodesTaxa = nodesTaxa;
            this.edges = edges;
            this.edgeLabels = edgeLabels;
            this.splitsByChar = splitsByChar;
            this.sort = sort;
            this.witness = witness;
        }
    }

    // Main entry point
    public static BuildResult run(CsvIO.Data data) throws NotPerfectPhylogenyException {
        SortResult sr = radixSortColumns(data.C, data.chars);
        BuildArtifacts A;
        try {
            A = buildArtifacts(sr.Csorted, sr.charsSorted, data.taxa);
        } catch (NotPerfectPhylogenyException ex) {
            throw new NotPerfectPhylogenyException(ex.getMessage(), ex.witnessChars, sr, ex.splitsByChar);
        }
        return new BuildResult(A.nodes, A.edges, A.edgeLabels, A.splitsByChar, sr, (A.conflict == null ? "OK" : A.conflict));
    }

    // Sort columns using true radix sort (lexicographic order, bottom-to-top)
    public static SortResult radixSortColumns(int[][] C, String[] chars) {
        int n = C.length; if (n == 0) throw new IllegalArgumentException("Empty matrix");
        int m = C[0].length;
        
        // Initialize column order
        Integer[] orderObj = new Integer[m];
        for (int j = 0; j < m; j++) orderObj[j] = j;
        
        // Radix sort: sort by each row from bottom (n-1) to top (0)
        for (int row = n - 1; row >= 0; row--) {
            final int currentRow = row;
            Arrays.sort(orderObj, (a, b) -> {
                // Compare bit at current row: 0 comes before 1 (stable sort)
                return Integer.compare(C[currentRow][a], C[currentRow][b]);
            });
        }
        
        // Build result arrays
        int[] order = new int[m];
        for (int j = 0; j < m; j++) order[j] = orderObj[j];
        int[][] sorted = new int[n][m];
        String[] cs = new String[m];
        for (int j = 0; j < m; j++) {
            int src = order[j];
            cs[j] = chars[src];
            for (int i = 0; i < n; i++) sorted[i][j] = C[i][src];
        }
        return new SortResult(sorted, cs, order);
    }

    // Build tree artifacts
    private static final class Block {
        final java.util.LinkedHashSet<Integer> taxa = new java.util.LinkedHashSet<>();
        Block(java.util.Collection<Integer> s) { this.taxa.addAll(s); }
    }
    private static final class BuildArtifacts {
        final List<Set<Integer>> nodes = new ArrayList<>();
        final List<int[]> edges = new ArrayList<>();
        final Map<Integer, List<String>> edgeLabels = new HashMap<>();
        final Map<String, Set<String>> splitsByChar = new java.util.LinkedHashMap<>();
        String conflict = null;
    }

    // Build the tree structure
    public static BuildArtifacts buildArtifacts(int[][] C, String[] chars, String[] taxaNames)
            throws NotPerfectPhylogenyException {
        int n = C.length, m = C[0].length;
        List<Block> blocks = new ArrayList<>();
        Block root = new Block(range(n));
        blocks.add(root);
        BuildArtifacts A = new BuildArtifacts();
        A.nodes.add(new java.util.LinkedHashSet<>(root.taxa));
        Map<Block, Integer> nodeId = new IdentityHashMap<>();
        nodeId.put(root, 0);

        // Process columns from last to first (reverse order for PP algorithm)
        for (int j = m - 1; j >= 0; j--) {
            java.util.LinkedHashSet<Integer> Oj = new java.util.LinkedHashSet<>();
            for (int i = 0; i < n; i++) if (C[i][j] == 1) Oj.add(i);
            if (Oj.isEmpty()) continue;
            List<Block> touched = new ArrayList<>();
            for (Block b : blocks) {
                if (!disjoint(b.taxa, Oj)) touched.add(b);
            }
            if (touched.size() == 0) {
                throw new NotPerfectPhylogenyException("Internal error: character " + chars[j] + " touches no block", List.of(chars[j]), null, A.splitsByChar);
            }
            if (touched.size() > 1) {
                throw new NotPerfectPhylogenyException("Conflict at character " + chars[j] + " (intersects multiple clades)", List.of(chars[j]), null, A.splitsByChar);
            }
            Block parent = touched.get(0);
            if (!parent.taxa.containsAll(Oj)) {
                throw new NotPerfectPhylogenyException("Conflict at character " + chars[j] + " (not contained in a single clade)", List.of(chars[j]), null, A.splitsByChar);
            }
            A.splitsByChar.put(chars[j], toTaxaNames(Oj, taxaNames));
            if (Oj.equals(parent.taxa)) continue;
            java.util.LinkedHashSet<Integer> rest = new java.util.LinkedHashSet<>(parent.taxa);
            rest.removeAll(Oj);
            int parentNode = nodeId.get(parent);
            A.nodes.set(parentNode, rest);
            int childNode = A.nodes.size();
            A.nodes.add(new java.util.LinkedHashSet<>(Oj));
            int eId = A.edges.size();
            A.edges.add(new int[]{parentNode, childNode});
            A.edgeLabels.computeIfAbsent(eId, k -> new ArrayList<>()).add(chars[j]);
            Block bRest = new Block(rest);
            Block bOj   = new Block(Oj);
            int idx = blocks.indexOf(parent);
            blocks.remove(idx);
            blocks.add(bRest);
            blocks.add(bOj);
            nodeId.remove(parent);
            nodeId.put(bRest, parentNode);
            nodeId.put(bOj, childNode);
        }
        return A;
    }

    // Helpers
    private static List<Integer> range(int n) { List<Integer> r = new ArrayList<>(n); for (int i=0;i<n;i++) r.add(i); return r; }
    private static boolean disjoint(Set<Integer> a, Set<Integer> b) {
        if (a.size() > b.size()) { Set<Integer> t = a; a = b; b = t; }
        for (Integer x : a) if (b.contains(x)) return false;
        return true;
    }
    private static Set<String> toTaxaNames(Set<Integer> idxs, String[] names) {
        java.util.LinkedHashSet<String> s = new java.util.LinkedHashSet<>();
        for (int i : idxs) s.add(names[i]);
        return s;
    }
}
