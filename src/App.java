import java.nio.file.*;
import java.util.*;

public class App {
  public static void main(String[] args) throws Exception {
    // 1) Read input
    CsvIO.Data data = CsvIO.read("data/input.csv");

    // 2) Run algorithm
    Algo.BuildResult res;
    try {
      res = Algo.run(data);
    } catch (Algo.NotPerfectPhylogenyException ex) {
      Files.createDirectories(Path.of("out"));
      // witness.txt
      Files.writeString(Path.of("out/witness.txt"),
          "NOT A PERFECT PHYLOGENY\nconflict: " + String.join(",", ex.witnessChars) + "\n");
      // tree_unrooted.nwk - ריק
      Files.writeString(Path.of("out/tree_unrooted.nwk"), "");
      // sorted_matrix.csv
      if (ex.sortResult != null) {
        StringBuilder sb = new StringBuilder();
        sb.append("taxon,").append(String.join(",", ex.sortResult.charsSorted)).append("\n");
        for (int i = 0; i < data.taxa.length; i++) {
          sb.append(data.taxa[i]);
          for (int j = 0; j < ex.sortResult.Csorted[0].length; j++) sb.append(",").append(ex.sortResult.Csorted[i][j]);
          sb.append("\n");
        }
        Files.writeString(Path.of("out/sorted_matrix.csv"), sb.toString());
      } else {
        Files.writeString(Path.of("out/sorted_matrix.csv"), "");
      }
      // splits.csv
      List<String> csv = ex.splitsByChar != null ? Tree.formatSplitsCsv(ex.splitsByChar) : new ArrayList<>();
      if (!csv.isEmpty()) {
        csv.set(0, csv.get(0) + ",NOT A PERFECT PHYLOGENY");
      }
      Files.write(Path.of("out/splits.csv"), csv);
      System.err.println("Conflict: " + ex.getMessage());
      return;
    }

    // 3) Build tree object
    Tree tree = Tree.fromArtifacts(res.nodesTaxa, res.edges, res.edgeLabels);

    // 4) Prepare outputs folder
    Files.createDirectories(Path.of("out"));

    // 4a) Build char->edgeId map for anchored Newick
    Map<String,Integer> charToEdgeId = new HashMap<>();
    for (int eId = 0; eId < res.edges.size(); eId++) {
      List<String> labels = res.edgeLabels.getOrDefault(eId, List.of());
      for (String L : labels) charToEdgeId.put(L, eId);
    }

    // 4b) Choose anchor & preferred inner split automatically from splits.csv info
    // Anchor = character with the largest non-trivial clade (not empty, not all taxa)
    String anchorChar = chooseAnchorChar(res.splitsByChar, data.taxa.length);
    // Prefer = largest split fully contained in the anchor side (proper subset)
    String preferChar = choosePreferChar(res.splitsByChar, anchorChar);

    // 4c) Newick (anchored if possible; fallback to default)
    String newick;
    try {
      newick = tree.toNewickAnchored(data.taxa, charToEdgeId, anchorChar, preferChar);
    } catch (Throwable t) {
      // If anchored printer not available, fall back to generic
      newick = tree.toNewick(data.taxa);
    }
    Files.writeString(Path.of("out/tree_unrooted.nwk"), newick);

    // 4d) splits.csv
    List<String> csv = Tree.formatSplitsCsv(res.splitsByChar);
    Files.write(Path.of("out/splits.csv"), csv);

    // 4e) witness
    Files.writeString(Path.of("out/witness.txt"), res.witness + "\n");

    // 4f) (optional) save sorted matrix for debugging
    StringBuilder sb = new StringBuilder();
    sb.append("taxon,").append(String.join(",", res.sort.charsSorted)).append("\n");
    for (int i = 0; i < data.taxa.length; i++) {
      sb.append(data.taxa[i]);
      for (int j = 0; j < res.sort.Csorted[0].length; j++) sb.append(",").append(res.sort.Csorted[i][j]);
      sb.append("\n");
    }
    Files.writeString(Path.of("out/sorted_matrix.csv"), sb.toString());

    System.out.println("Done. Newick in out/tree_unrooted.nwk");
    System.out.println("Anchor = " + anchorChar + ", PreferInside = " + preferChar);
  }

  // ---------- Helpers to select anchor/prefer characters deterministically ----------

  private static String chooseAnchorChar(Map<String, Set<String>> splitsByChar, int nTaxa) {
    String bestChar = null;
    int bestSize = -1;

    for (Map.Entry<String, Set<String>> e : splitsByChar.entrySet()) {
      int sz = e.getValue().size();
      if (sz == 0 || sz == nTaxa) continue; // trivial split; ignore
      if (sz > bestSize || (sz == bestSize && e.getKey().compareTo(bestChar) < 0)) {
        bestChar = e.getKey();
        bestSize = sz;
      }
    }
    // fallback: if none found (all-zero or trivial), just pick lexicographically smallest key
    if (bestChar == null && !splitsByChar.isEmpty()) {
      bestChar = splitsByChar.keySet().stream().sorted().findFirst().orElse(null);
    }
    return bestChar;
  }

  private static String choosePreferChar(Map<String, Set<String>> splitsByChar, String anchorChar) {
    if (anchorChar == null) return null;
    Set<String> anchorSet = splitsByChar.get(anchorChar);
    if (anchorSet == null) return null;

    String best = null;
    int bestSize = -1;

    for (Map.Entry<String, Set<String>> e : splitsByChar.entrySet()) {
      String ch = e.getKey();
      if (ch.equals(anchorChar)) continue;
      Set<String> s = e.getValue();
      if (s.isEmpty()) continue;
      // prefer only proper subsets of the anchor side
      if (anchorSet.containsAll(s) && s.size() < anchorSet.size()) {
        if (s.size() > bestSize || (s.size() == bestSize && ch.compareTo(best) < 0)) {
          best = ch;
          bestSize = s.size();
        }
      }
    }
    return best;
  }
}
