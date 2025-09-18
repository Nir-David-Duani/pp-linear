import java.nio.file.*;
import java.util.*;

public class App {
// Helper to choose anchor character
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
    if (bestChar == null && !splitsByChar.isEmpty()) {
        bestChar = splitsByChar.keySet().stream().sorted().findFirst().orElse(null);
    }
    return bestChar;
}

// Helper to choose preferred character
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
        if (anchorSet.containsAll(s) && s.size() < anchorSet.size()) {
            if (s.size() > bestSize || (s.size() == bestSize && ch.compareTo(best) < 0)) {
                best = ch;
                bestSize = s.size();
            }
        }
    }
    return best;
}
    public static void main(String[] args) throws Exception {
    // Read input data from CSV file
    CsvIO.Data data = CsvIO.read("data/input.csv");

    // Run the main algorithm
    Algo.BuildResult result;
    try {
        result = Algo.run(data);
    } catch (Algo.NotPerfectPhylogenyException ex) {
        // Create output folder if needed
        Files.createDirectories(Path.of("out"));
        // Write witness file for error case
        Files.writeString(Path.of("out/witness.txt"), "NOT A PERFECT PHYLOGENY\nconflict: " + String.join(",", ex.witnessChars) + "\n");
        // Write empty tree file
        Files.writeString(Path.of("out/tree_unrooted.nwk"), "");
        // Write sorted matrix if available
        if (ex.sortResult != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("taxon,");
            sb.append(String.join(",", ex.sortResult.charsSorted));
            sb.append("\n");
            for (int i = 0; i < data.taxa.length; i++) {
                sb.append(data.taxa[i]);
                for (int j = 0; j < ex.sortResult.Csorted[0].length; j++) {
                    sb.append(",").append(ex.sortResult.Csorted[i][j]);
                }
                sb.append("\n");
            }
            Files.writeString(Path.of("out/sorted_matrix.csv"), sb.toString());
        } else {
            Files.writeString(Path.of("out/sorted_matrix.csv"), "");
        }
        // Write splits file with error note
        List<String> csv = ex.splitsByChar != null ? Tree.formatSplitsCsv(ex.splitsByChar) : new ArrayList<>();
        if (!csv.isEmpty()) {
            csv.set(0, csv.get(0) + ",NOT A PERFECT PHYLOGENY");
        }
        Files.write(Path.of("out/splits.csv"), csv);
        System.err.println("Conflict: " + ex.getMessage());
        return;
    }

    // Build tree from results
    Tree tree = Tree.fromArtifacts(result.nodesTaxa, result.edges, result.edgeLabels);
    Files.createDirectories(Path.of("out"));

    // Prepare edge labels for Newick format
    Map<String, Integer> charToEdgeId = new HashMap<>();
    for (int eId = 0; eId < result.edges.size(); eId++) {
        List<String> labels = result.edgeLabels.getOrDefault(eId, List.of());
        for (String label : labels) {
            charToEdgeId.put(label, eId);
        }
    }

    // Choose anchor and preferred character for Newick
    String anchorChar = chooseAnchorChar(result.splitsByChar, data.taxa.length);
    String preferChar = choosePreferChar(result.splitsByChar, anchorChar);

    // Write tree in Newick format
    String newick;
    try {
        newick = tree.toNewickAnchored(data.taxa, charToEdgeId, anchorChar, preferChar);
    } catch (Exception e) {
        newick = tree.toNewick(data.taxa);
    }
    Files.writeString(Path.of("out/tree_unrooted.nwk"), newick);

    // Write splits file
    List<String> csv = Tree.formatSplitsCsv(result.splitsByChar);
    Files.write(Path.of("out/splits.csv"), csv);

    // Write witness file
    Files.writeString(Path.of("out/witness.txt"), result.witness + "\n");

    // Write sorted matrix file
    StringBuilder sb = new StringBuilder();
    sb.append("taxon,");
    sb.append(String.join(",", result.sort.charsSorted));
    sb.append("\n");
    for (int i = 0; i < data.taxa.length; i++) {
        sb.append(data.taxa[i]);
        for (int j = 0; j < result.sort.Csorted[0].length; j++) {
            sb.append(",").append(result.sort.Csorted[i][j]);
        }
        sb.append("\n");
    }
    Files.writeString(Path.of("out/sorted_matrix.csv"), sb.toString());

    // Print summary to console
    System.out.println("Done. Newick in out/tree_unrooted.nwk");
    System.out.println("Anchor = " + anchorChar + ", PreferInside = " + preferChar);
}

    // Run algorithm with custom output directory
    public static void runWithOutputDir(String inputFile, String outputDir) throws Exception {
        // Read input data from CSV file
        CsvIO.Data data = CsvIO.read(inputFile);

        // Run the main algorithm
        Algo.BuildResult result;
        try {
            result = Algo.run(data);
        } catch (Algo.NotPerfectPhylogenyException ex) {
            // Create output folder if needed
            Files.createDirectories(Path.of(outputDir));
            // Write witness file for error case
            Files.writeString(Path.of(outputDir + "/witness.txt"), "NOT A PERFECT PHYLOGENY\nconflict: " + String.join(",", ex.witnessChars) + "\n");
            // Write empty tree file
            Files.writeString(Path.of(outputDir + "/tree_unrooted.nwk"), "");
            // Write sorted matrix if available
            if (ex.sortResult != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("taxon,");
                sb.append(String.join(",", ex.sortResult.charsSorted));
                sb.append("\n");
                for (int i = 0; i < data.taxa.length; i++) {
                    sb.append(data.taxa[i]);
                    for (int j = 0; j < ex.sortResult.Csorted[0].length; j++) {
                        sb.append(",").append(ex.sortResult.Csorted[i][j]);
                    }
                    sb.append("\n");
                }
                Files.writeString(Path.of(outputDir + "/sorted_matrix.csv"), sb.toString());
            } else {
                Files.writeString(Path.of(outputDir + "/sorted_matrix.csv"), "");
            }
            // Write splits file with error note
            List<String> csv = ex.splitsByChar != null ? Tree.formatSplitsCsv(ex.splitsByChar) : new ArrayList<>();
            if (!csv.isEmpty()) {
                csv.set(0, csv.get(0) + ",NOT A PERFECT PHYLOGENY");
            }
            Files.write(Path.of(outputDir + "/splits.csv"), csv);
            System.err.println("Test failed - Conflict: " + ex.getMessage());
            return;
        }

        // Build tree from results
        Tree tree = Tree.fromArtifacts(result.nodesTaxa, result.edges, result.edgeLabels);
        Files.createDirectories(Path.of(outputDir));

        // Prepare edge labels for Newick format
        Map<String, Integer> charToEdgeId = new HashMap<>();
        for (int eId = 0; eId < result.edges.size(); eId++) {
            List<String> labels = result.edgeLabels.getOrDefault(eId, List.of());
            for (String label : labels) {
                charToEdgeId.put(label, eId);
            }
        }

        // Choose anchor and preferred character for Newick
        String anchorChar = chooseAnchorChar(result.splitsByChar, data.taxa.length);
        String preferChar = choosePreferChar(result.splitsByChar, anchorChar);

        // Write tree in Newick format
        String newick;
        try {
            newick = tree.toNewickAnchored(data.taxa, charToEdgeId, anchorChar, preferChar);
        } catch (Exception e) {
            newick = tree.toNewick(data.taxa);
        }
        Files.writeString(Path.of(outputDir + "/tree_unrooted.nwk"), newick);

        // Write splits file
        List<String> csv = Tree.formatSplitsCsv(result.splitsByChar);
        Files.write(Path.of(outputDir + "/splits.csv"), csv);

        // Write witness file
        Files.writeString(Path.of(outputDir + "/witness.txt"), result.witness + "\n");

        // Write sorted matrix file
        StringBuilder sb = new StringBuilder();
        sb.append("taxon,");
        sb.append(String.join(",", result.sort.charsSorted));
        sb.append("\n");
        for (int i = 0; i < data.taxa.length; i++) {
            sb.append(data.taxa[i]);
            for (int j = 0; j < result.sort.Csorted[0].length; j++) {
                sb.append(",").append(result.sort.Csorted[i][j]);
            }
            sb.append("\n");
        }
        Files.writeString(Path.of(outputDir + "/sorted_matrix.csv"), sb.toString());
    }


}
