import java.nio.file.*;
import java.util.*;

public class App {
  public static void main(String[] args) throws Exception {
    // 1) read input
    CsvIO.Data data = CsvIO.read("data/input.csv");

    // 2) run algorithm
    Algo.BuildResult res;
    try {
      res = Algo.run(data);
    } catch (Algo.NotPerfectPhylogenyException ex) {
      // write witness and exit
      Files.createDirectories(Path.of("out"));
      Files.writeString(Path.of("out/witness.txt"),
          "NOT A PERFECT PHYLOGENY\nconflict: " + String.join(",", ex.witnessChars) + "\n");
      System.err.println("Conflict: " + ex.getMessage());
      return;
    }

    // 3) build tree object
    Tree tree = Tree.fromArtifacts(res.nodesTaxa, res.edges, res.edgeLabels);

    // 4) write outputs
    Files.createDirectories(Path.of("out"));

    // 4a) Newick
    String newick = tree.toNewick(data.taxa);
    Files.writeString(Path.of("out/tree_unrooted.nwk"), newick);

    // 4b) splits.csv
    List<String> csv = Tree.formatSplitsCsv(res.splitsByChar);
    Files.write(Path.of("out/splits.csv"), csv);

    // 4c) witness
    Files.writeString(Path.of("out/witness.txt"), res.witness + "\n");

    // 4d) (optional) save sorted matrix for debugging
    StringBuilder sb = new StringBuilder();
    sb.append("taxon,").append(String.join(",", res.sort.charsSorted)).append("\n");
    for (int i = 0; i < data.taxa.length; i++) {
      sb.append(data.taxa[i]);
      for (int j = 0; j < res.sort.Csorted[0].length; j++) sb.append(",").append(res.sort.Csorted[i][j]);
      sb.append("\n");
    }
    Files.writeString(Path.of("out/sorted_matrix.csv"), sb.toString());

    System.out.println("Done. Newick in out/tree_unrooted.nwk");
  }
}
