import java.util.*;

// Simple tree structure for phylogeny
public final class Tree {

    public static final class Node {
        public final int id;
        public final Set<Integer> taxa = new LinkedHashSet<>();
        public final List<Integer> neighbors = new ArrayList<>();
        Node(int id) { this.id = id; }
    }

    public static final class Edge {
        public final int u, v;
        public final List<String> labels;
        Edge(int u, int v, List<String> labels) { this.u=u; this.v=v; this.labels = labels; }
    }

    public final List<Node> nodes = new ArrayList<>();
    public final List<Edge> edges = new ArrayList<>();

    // Build tree from artifacts
    public static Tree fromArtifacts(List<Set<Integer>> nodesTaxa, List<int[]> edges, Map<Integer, List<String>> edgeLabels) {
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

    // Write tree in Newick format
    public String toNewick(String[] taxaNames) {
        int root = 0;
        for (Node nd : nodes) {
            if (nd.neighbors.size() != 2) { root = nd.id; break; }
        }
        boolean[] seen = new boolean[nodes.size()];
        return dfsNewick(root, -1, seen, taxaNames) + ";";
    }

    private String dfsNewick(int u, int parent, boolean[] seen, String[] taxaNames) {
        seen[u] = true;
        List<String> parts = new ArrayList<>();
        for (int ti : nodes.get(u).taxa) parts.add(taxaNames[ti]);
        for (int v : nodes.get(u).neighbors) {
            if (v == parent) continue;
            parts.add(dfsNewick(v, u, seen, taxaNames));
        }
        if (parts.size() > 1) {
            return "(" + String.join(",", parts) + ")";
        }
        if (parts.size() == 1) return parts.get(0);
        return "";
    }

    // Write splits.csv
    public static List<String> formatSplitsCsv(Map<String, Set<String>> splits) {
        List<String> out = new ArrayList<>();
        out.add("character,clade");
        for (var e : splits.entrySet()) {
            List<String> list = new ArrayList<>(e.getValue());
            Collections.sort(list);
            String charId = e.getKey().startsWith("C") ? e.getKey().substring(1) : e.getKey();
            out.add(charId + "," + String.join("", list));
        }
        return out;
    }

    // Anchored Newick writer (optional, can keep as is)
    /** 
     * Render Newick anchored on a specific character edge (e.g., "C2"),
     * and prefer a specific inner split when ordering (e.g., "C3").
     * Example: toNewickAnchored(..., "C2", "C3") -> "(((A,B),C),(D,E));" for the classic case.
     */
    public String toNewickAnchored(String[] taxaNames,
                                   Map<String,Integer> charToEdgeId,
                                   String anchorChar,
                                   String preferInsideChar) {
      Integer eId = charToEdgeId.get(anchorChar);
      if (eId == null) {
        // Fallback: default writer if anchor not found
        return toNewick(taxaNames);
      }
      Edge anchor = edges.get(eId);
  
      Subtree left  = formatSubtree(anchor.u, anchor.v, preferInsideChar, taxaNames, new boolean[nodes.size()]);
      Subtree right = formatSubtree(anchor.v, anchor.u, preferInsideChar, taxaNames, new boolean[nodes.size()]);
  
      // Put the larger side first; tie-break lexicographically by leaf names
      Subtree first = left, second = right;
      if (right.leaves.size() > left.leaves.size() ||
          (right.leaves.size() == left.leaves.size() && joinLex(right.leaves).compareTo(joinLex(left.leaves)) < 0)) {
        first = right; second = left;
      }
      return "(" + first.newick + "," + second.newick + ");";
    }
  
    /** Internal struct for deterministic ordering. */
    private static final class Subtree {
      final String newick;
      final List<String> leaves;
      final boolean hasPrefer; // this subtree/edge contains preferInsideChar
      Subtree(String n, List<String> l, boolean p) { newick = n; leaves = l; hasPrefer = p; }
    }
  
    /** 
     * Build a subtree string without crossing the blocked anchor-edge.
     * @param u current node id; @param block neighbor that is "blocked" (do not cross u<->block)
     */
    private Subtree formatSubtree(int u, int block, String preferChar, String[] taxaNames, boolean[] seen) {
      seen[u] = true;
  
      // Start with leaves attached at this node
      List<String> parts = new ArrayList<>();
      List<String> myLeaves = new ArrayList<>();
      for (int ti : nodes.get(u).taxa) {
        String name = taxaNames[ti];
        parts.add(name);
        myLeaves.add(name);
      }
  
      // Recurse to children, skipping the blocked neighbor and visited parent
      List<Subtree> subs = new ArrayList<>();
      for (int v : nodes.get(u).neighbors) {
        if (v == block) continue;
        if (seen[v]) continue;
  
        // Check if edge (u,v) carries preferChar
        boolean edgeHasPrefer = false;
        for (Edge e : edges) {
          if ((e.u == u && e.v == v) || (e.u == v && e.v == u)) {
            if (preferChar != null && e.labels.contains(preferChar)) edgeHasPrefer = true;
            break;
          }
        }
  
        Subtree st = formatSubtree(v, u, preferChar, taxaNames, seen);
        subs.add(new Subtree(st.newick, st.leaves, edgeHasPrefer || st.hasPrefer));
        myLeaves.addAll(st.leaves);
      }
  
      // Deterministic ordering of child subtrees:
      // 1) ones that contain preferChar come first
      // 2) then by size (more leaves first)
      // 3) then lexicographically
      subs.sort((a,b) -> {
        if (a.hasPrefer != b.hasPrefer) return a.hasPrefer ? -1 : 1;
        if (a.leaves.size() != b.leaves.size()) return Integer.compare(b.leaves.size(), a.leaves.size());
        return joinLex(a.leaves).compareTo(joinLex(b.leaves));
      });
  
      for (Subtree st : subs) parts.add(st.newick);
  
      String newick = (parts.size() == 1) ? parts.get(0) : "(" + String.join(",", parts) + ")";
  
      Collections.sort(myLeaves);
      boolean hasPrefer = subs.stream().anyMatch(s -> s.hasPrefer);
      return new Subtree(newick, myLeaves, hasPrefer);
    }
  
    private static String joinLex(List<String> xs) {
      List<String> ss = new ArrayList<>(xs);
      Collections.sort(ss);
      return String.join("|", ss);
    }
  
  }
