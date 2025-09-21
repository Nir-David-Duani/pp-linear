import java.nio.file.*;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        String testsDir = "tests";
        String resultsDir = testsDir + "/results";
        
        // Create results directory
        Files.createDirectories(Paths.get(resultsDir));
        
        // Run all tests
        for (int i = 1; i <= 6; i++) {
            String inputFile = testsDir + "/test" + i + ".csv";
            String outputDir = resultsDir + "/test" + i;
            
            if (Files.exists(Paths.get(inputFile))) {
                runTest(inputFile, outputDir, i);
            }
        }
        
        System.out.println("All tests completed!");
    }
    
    private static void runTest(String inputFile, String outputDir, int testNum) {
        try {
            // Create test output directory
            Files.createDirectories(Paths.get(outputDir));
            
            // Run the algorithm with custom output directory
            App.runWithOutputDir(inputFile, outputDir);
            
            System.out.println("Test " + testNum + " completed successfully");
            
        } catch (Exception e) {
            System.out.println("Test " + testNum + " failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
