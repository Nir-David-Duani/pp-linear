import java.util.*;

/**
 * Unrooted Perfect Phylogeny in O(n*m).
 * 1) radixSortColumns: stable bottom-up radix; per row put '1' before '0' (supersets first).
 * 2) buildArtifacts: iterate columns left->right; maintain a partition of taxa (blocks),
 *    do split / co-label / conflict; construct graph artifacts for Tree + splits.
 */
public final class Algo {

  /* ========================
   * Exceptions & Data Types
   * ======================== */

  /** Thrown when input is not a perfect phylogeny. */
  public static final class NotPerfectPhylogenyException extends Exception {
    public final List<String> witnessChars; // minimal set if you have it, else singleton
    public final SortResult sortResult;
    public final Map<String, Set<String>> splitsByChar;
    public NotPerfectPhylogenyException(String message, List<String> witnessChars, SortResult sortResult, Map<String, Set<String>> splitsByChar) {
      super(message);
      this.witnessChars = witnessChars;
      this.sortResult = sortResult;
      this.splitsByChar = splitsByChar;
    }
  }

  /** Result of the radix sort. */
  public static final class SortResult {
    public final int[][] Csorted;        // n x m matrix with columns permuted
    public final String[] charsSorted;   // character names in sorted order
    public final int[] columnOrder;      // columnOrder[k] = original index for sorted column k (0-based)
    public SortResult(int[][] Csorted, String[] charsSorted, int[] columnOrder) {
      this.Csorted = Csorted; this.charsSorted = charsSorted; this.columnOrder = columnOrder;
    }
  }

  /** Final artifacts to feed Tree + csv writers + witness. */
  public static final class BuildResult {
    // Graph artifacts for Tree.fromArtifacts(...)
    public final List<Set<Integer>> nodesTaxa;          // nodeId -> set of taxa indices in that block
    public final List<int[]> edges;                     // undirected edges [u,v]
    public final Map<Integer, List<String>> edgeLabels; // edgeId -> list of character labels (from splits)

    // For splits.csv
    public final Map<String, Set<String>> splitsByChar; // "Cj" -> taxa name set for Oj

    // Sorting info
    public final SortResult sort;

    // Witness text ("OK" or explanation)
    public final String witness;

    public BuildResult(
        List<Set<Integer>> nodesTaxa,
        List<int[]> edges,
        Map<Integer, List<String>> edgeLabels,
        Map<String, Set<String>> splitsByChar,
        SortResult sort,
        String witness
    ) {
      this.nodesTaxa = nodesTaxa;
      this.edges = edges;
      this.edgeLabels = edgeLabels;
      this.splitsByChar = splitsByChar;
      this.sort = sort;
      this.witness = witness;
    }
  }

  /* ====================
   * Public entry point
   * ==================== */

  /** Full pipeline: radix sort -> iterative build (first->last). */
  public static BuildResult run(CsvIO.Data data) throws NotPerfectPhylogenyException {
    SortResult sr = radixSortColumns(data.C, data.chars);
    BuildArtifacts A;
    try {
      A = buildArtifacts(sr.Csorted, sr.charsSorted, data.taxa);
    } catch (NotPerfectPhylogenyException ex) {
      // rethrow with sortResult and splitsByChar
      throw new NotPerfectPhylogenyException(ex.getMessage(), ex.witnessChars, sr, ex.splitsByChar);
    }
    return new BuildResult(A.nodes, A.edges, A.edgeLabels, A.splitsByChar, sr, (A.conflict == null ? "OK" : A.conflict));
  }

  /* =========================
   * Step 1: stable radix sort
   * ========================= */

  /**
   * Stable bottom-up radix: for each row from bottom to top, do a stable counting pass
   * where bucket(1) comes BEFORE bucket(0). That pushes columns with more/high 1-patterns
   * earlier in lexicographic order => supersets (larger 1-sets) appear first (leftmost).
   */
  public static SortResult radixSortColumns(int[][] C, String[] chars) {
    int n = C.length; if (n == 0) throw new IllegalArgumentException("Empty matrix");
    int m = C[0].length;

    int[] order = new int[m];
    for (int j = 0; j < m; j++) order[j] = j;

    int[] tmp = new int[m];
    for (int row = n - 1; row >= 0; row--) {
      int ones = 0; for (int idx : order) if (C[row][idx] == 1) ones++;
      int pos1 = 0, pos0 = ones; // 1's first, then 0's (stable)
      for (int idx : order) {
        if (C[row][idx] == 1) tmp[pos1++] = idx;
        else                  tmp[pos0++] = idx;
      }
      order = java.util.Arrays.copyOf(tmp, m);
    }

    int[][] sorted = new int[n][m];
    String[] cs = new String[m];
    for (int j = 0; j < m; j++) {
      int src = order[j];
      cs[j] = chars[src];
      for (int i = 0; i < n; i++) sorted[i][j] = C[i][src];
    }
    return new SortResult(sorted, cs, order);
  }

  /* ==========================
   * Step 2: iterative building
   * ========================== */

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

  /** Partition refinement from first->last column; builds graph artifacts. */
  public static BuildArtifacts buildArtifacts(int[][] C, String[] chars, String[] taxaNames)
  throws NotPerfectPhylogenyException {
    int n = C.length, m = C[0].length;

    // initial single block with all taxa
    List<Block> blocks = new ArrayList<>();
    Block root = new Block(range(n));
    blocks.add(root);

    // graph nodes: node 0 represents the current "rest" world (initially all taxa)
    BuildArtifacts A = new BuildArtifacts();
    A.nodes.add(new java.util.LinkedHashSet<>(root.taxa)); // node 0
    Map<Block, Integer> nodeId = new IdentityHashMap<>();
    nodeId.put(root, 0);

    // process columns first -> last
    for (int j = 0; j < m; j++) {
      java.util.LinkedHashSet<Integer> Oj = new java.util.LinkedHashSet<>();
      for (int i = 0; i < n; i++) if (C[i][j] == 1) Oj.add(i);
      if (Oj.isEmpty()) continue; // ignore all-zero

      // find touched blocks
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

      // always record split for splits.csv
      A.splitsByChar.put(chars[j], toTaxaNames(Oj, taxaNames));

      if (Oj.equals(parent.taxa)) {
        // co-label case: no structural change
        continue;
      }

      // split parent into Rest and Oj
      java.util.LinkedHashSet<Integer> rest = new java.util.LinkedHashSet<>(parent.taxa);
      rest.removeAll(Oj);

      // reuse parent's node for Rest (keeps prior connectivity)
      int parentNode = nodeId.get(parent);
      A.nodes.set(parentNode, rest);

      // new node for Oj
      int childNode = A.nodes.size();
      A.nodes.add(new java.util.LinkedHashSet<>(Oj));

      // add undirected edge labeled with current character
      int eId = A.edges.size();
      A.edges.add(new int[]{parentNode, childNode});
      A.edgeLabels.computeIfAbsent(eId, k -> new ArrayList<>()).add(chars[j]);

      // update partition
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

  /* ============
   * Small helpers
   * ============ */
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
