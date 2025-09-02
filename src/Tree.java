import java.util.*;

/** Minimal unrooted tree with labeled edges (co-labels supported). */
public final class Tree {

  public static final class Node {
    public final int id;
    public final Set<Integer> taxa = new LinkedHashSet<>(); // indices of taxa in this block
    public final List<Integer> neighbors = new ArrayList<>();
    Node(int id) { this.id = id; }
  }

  public static final class Edge {
    public final int u, v;                  // node ids
    public final List<String> labels;       // characters labeling this split (co-labels)
    Edge(int u, int v, List<String> labels) { this.u=u; this.v=v; this.labels = labels; }
  }

  public final List<Node> nodes = new ArrayList<>();
  public final List<Edge> edges = new ArrayList<>();

  /** Build Tree from Algo artifacts. */
  public static Tree fromArtifacts(
      List<Set<Integer>> nodesTaxa,
      List<int[]> edges,
      Map<Integer, List<String>> edgeLabels
  ) {
    Tree t = new Tree();
    for (int i = 0; i < nodesTaxa.size(); i++) {
      Node nd = new Node(i);
      nd.taxa.addAll(nodesTaxa.get(i));
      t.nodes.add(nd);
    }
    for (int eId = 0; eId < edges.size(); eId++) {
      int[] e = edges.get(eId);
      int u = e[0], v = e[1];
      List<String> labels = edgeLabels.getOrDefault(eId, List.of());
      t.edges.add(new Edge(u, v, new ArrayList<>(labels)));
      t.nodes.get(u).neighbors.add(v);
      t.nodes.get(v).neighbors.add(u);
    }
    return t;
  }

  /** Render as Newick using any node of degree != 2 as root anchor (fallback: 0). */
  public String toNewick(String[] taxaNames) {
    // pick anchor
    int root = 0;
    for (Node nd : nodes) {
      if (nd.neighbors.size() != 2) { root = nd.id; break; }
    }
    // DFS to produce Newick; treat blocks as internal and print leaves under them
    boolean[] seen = new boolean[nodes.size()];
    return dfsNewick(root, -1, seen, taxaNames) + ";";
  }

  private String dfsNewick(int u, int parent, boolean[] seen, String[] taxaNames) {
    seen[u] = true;
    List<String> parts = new ArrayList<>();

    // attach taxa at this block as leaves
    for (int ti : nodes.get(u).taxa) parts.add(taxaNames[ti]);

    for (int v : nodes.get(u).neighbors) {
      if (v == parent) continue;
      parts.add(dfsNewick(v, u, seen, taxaNames));
    }

    if (parts.size() == 1) return parts.get(0);       // leaf or single taxon
    return "(" + String.join(",", parts) + ")";       // internal
  }

  /** Write splits.csv lines: character,clade (e.g., "2,ABC"). */
  public static List<String> formatSplitsCsv(Map<String, Set<String>> splits) {
    List<String> out = new ArrayList<>();
    out.add("character,clade");
    for (var e : splits.entrySet()) {
      // clade as concatenated names in stable order
      List<String> list = new ArrayList<>(e.getValue());
      Collections.sort(list);
      // strip leading 'C' from "Cj" for the numeric field in CSV, per your examples
      String charId = e.getKey().startsWith("C") ? e.getKey().substring(1) : e.getKey();
      out.add(charId + "," + String.join("", list));
    }
    return out;
  }
}
