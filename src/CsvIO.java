public class CsvIO {
    public static class Data {
        public String[] taxa;
        public String[] chars;
        public int[][] C;

        public Data() {}

        public Data(String[] taxa, String[] chars, int[][] C) {
            this.taxa = taxa;
            this.chars = chars;
            this.C = C;
        }
    }

    public static Data read(String filename) throws Exception {
        // Prepare lists for taxa and rows
        java.util.List<String> taxaList = new java.util.ArrayList<>();
        java.util.List<int[]> rows = new java.util.ArrayList<>();

        // Read file safely
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filename))) {
            // Read header
            String headerLine = br.readLine();
            if (headerLine == null)
                throw new Exception("Empty file");
            String[] header = headerLine.split(",");
            if (header.length < 2 || !header[0].equals("taxon"))
                throw new Exception("Header must start with 'taxon'");
            String[] chars = java.util.Arrays.copyOfRange(header, 1, header.length);

            // Validate character names: must be C1, C2, C3, ...
            for (int i = 0; i < chars.length; i++) {
                if (!chars[i].matches("C\\d+")) {
                    throw new Exception("Character names must be C1, C2, C3, ... only (found: '" + chars[i] + "')");
                }
                if (!chars[i].equals("C" + (i + 1))) {
                    throw new Exception("Character names must be sequential: C1, C2, C3, ... (found: '" + chars[i] + "' at position " + (i + 1) + ")");
                }
            }

            // Read data rows
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(",");
                if (cols.length != header.length)
                    throw new Exception("Row length mismatch");
                taxaList.add(cols[0]);
                int[] feats = new int[chars.length];
                for (int i = 1; i < cols.length; i++) {
                    if (!cols[i].equals("0") && !cols[i].equals("1"))
                        throw new Exception("Feature must be 0 or 1");
                    feats[i - 1] = Integer.parseInt(cols[i]);
                }
                rows.add(feats);
            }

            // Build Data object using constructor
            return new Data(
                taxaList.toArray(new String[0]),
                chars,
                rows.toArray(new int[0][])
            );
        }
    }
}