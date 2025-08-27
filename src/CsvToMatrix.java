import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.io.File;
import java.util.Scanner;


/** converting the csv format input into matrix, represented in a 2 dimensional integer array */
public final class CsvToMatrix {

    // main function to be called, returns the matrix if the file is valid
    public int[][] buildMatrix (Path inputCsv) throws IOException, CsvFormatException {
        Constants cons = check(inputCsv);
        return builder (inputCsv, ',', cons.num_of_features, cons.num_of_taxons);        
    }

    // helper function to get number of features. delimiter is assumed to be ','

    // checks the validity of the csv file, returns number of taxons (rows)
    private static Constants check (Path csvFile) throws IOException, CsvFormatException {
        File file = checkFile(csvFile);
        return checkContent(file);
    }

    // builds the matrix from the csv file, assumes the file is valid
    private static int[][] builder (Path csvFile, char delimiter, int numOfFeatures, int numOfTaxons) throws IOException, CsvFormatException {
        int[][] matrix = new int[numOfFeatures][numOfTaxons];
        try (Scanner sc = new Scanner(csvFile.toFile(), "UTF-8")) {
            // skipping the header
            sc.nextLine();
            int rowIndex = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.isBlank()) continue; // skipping empty lines
                String[] cols = line.split(String.valueOf(delimiter), -1); 
                for (int colIndex = 1; colIndex < numOfFeatures + 1; colIndex++) {
                    int value = Integer.parseInt(cols[colIndex]);
                    matrix[colIndex-1][rowIndex] = value;
                }
                rowIndex++; 
            }
            if (rowIndex != numOfTaxons) {
                throw new CsvFormatException("Row count mismatch: expected " + numOfTaxons + ", but got " + rowIndex);
            }
        }
        return matrix; 
    }

    // checking the file, returns the file if valid
    private static File checkFile (Path csvFile) throws IOException {
        // checking if the file exists
        if (!csvFile.toFile().exists()){
            throw new IOException("File does not exist: " + csvFile);
        }

        // checking if the path is indeed a file
        if (!csvFile.toFile().isFile()) {
            throw new IOException(csvFile + "is not a file"); 
        }

        // checking if the file is readable 
        if (!csvFile.toFile().canRead()) {
            throw new IOException("File is not readable: " + csvFile); 
        }

        return csvFile.toFile();
    }

    // checking the content of the file, returns number of features (columns-1) and number of taxons (rows)
    private static Constants checkContent (File file) throws IOException, CsvFormatException {
        try (Scanner sc = new Scanner(file, StandardCharsets.UTF_8)) {
            int lineNum = 0; 

            if (!sc.hasNextLine()) {
                throw new CsvFormatException("File is empty"); 
            }

            // checking the header
            String headerLine = sc.nextLine(); 
            lineNum++;
            int numOfFeatures = checkHeader(headerLine, ','); 

            // checking data row-by-row
            boolean hasDataRow = false;
            while (sc.hasNextLine()) {
                String line = sc.nextLine(); 
                lineNum++;
                checkRow(line, ',', lineNum, numOfFeatures);
                hasDataRow = true;
            }

            if (!hasDataRow) {
                throw new CsvFormatException("No data rows found in the file");
            }

            Constants answer = new Constants(numOfFeatures, lineNum - 1);
            return answer;
        }
    }

    // checking the header, returns the number of features
    private static int checkHeader (String headerLine, char delimiter) throws CsvFormatException {
        if (headerLine == null || headerLine.isEmpty()) {
            throw new CsvFormatException(headerLine + " is not a valid header");
        }

        String[] header = headerLine.split(String.valueOf(delimiter), -1);

        if (header.length < 2) {
            throw new CsvFormatException("Header must contain at least 2 columns - taxon and one feature");
        }

        if (!header[0].equals("taxon")) {
            throw new CsvFormatException("Header: first column must be 'taxon'");
        }

        java.util.Set<String> features = new java.util.HashSet<>();
        for (int i = 1; i < header.length; i++) {
            String curFeat = header[i];
            if (curFeat.isEmpty()) {
                throw new CsvFormatException("Header: feature name in column " + (i + 1) + " is empty");
            }
            if (!features.add(curFeat)) {
                throw new CsvFormatException("Header: feature name '" + curFeat + "' is duplicated");
            }
        }
        return header.length - 1; 

    }

    private static void checkRow (String row, char delimiter, int rowNum, int numOfFeaturs) throws CsvFormatException{
        if (row == null || row.isEmpty()) {
            throw new CsvFormatException("Row " + rowNum + " is empty");
        }

        String[] cols = row.split(String.valueOf(delimiter), -1);

        if (cols.length != numOfFeaturs + 1) {
            throw new CsvFormatException("Row " + rowNum + ": expected " + (numOfFeaturs + 1) + " columns"); 
        }

        if (cols[0] == null && cols[0].isEmpty()) {
            throw new CsvFormatException("Row " + rowNum + ": taxon name is empty");
        } 

        for (int i = 1; i < cols.length; i++) {
            if (!cols[i].equals("0") || !cols[i].equals("1")) {
                throw new CsvFormatException("Row " + rowNum + ": feature value in column " + (i + 1) + " is not '0' or '1' ");
            }
        }
    }
    

    // user-friendly error format 
    public static class CsvFormatException extends Exception {
        public CsvFormatException(String msg) { super(msg); }
        public CsvFormatException(String msg, Throwable cause) { super(msg, cause); }
    }
    
    // a simple struct to hold the constants
    private static final class Constants {
        final int num_of_taxons;
        final int num_of_features;
    
        private Constants(int features, int taxons) {
            this.num_of_features = features;
            this.num_of_taxons = taxons;
        }
    }
}
