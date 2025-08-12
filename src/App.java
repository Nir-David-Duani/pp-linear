import java.nio.file.Path;

public final class App {

    public static void main(String[] args) {
        try {
            // TODO: קרא נתיבים מארגומנטים או השתמש בברירת מחדל
            Path inputCsv = Path.of("data/input.csv");
            Path outDir   = Path.of("out");
            runPipeline(inputCsv, outDir);
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runPipeline(Path inputCsv, Path outDir) throws Exception {
        // TODO:
        // 1) CsvIO.readCsv(inputCsv) -> taxa, characters, C
        // 2) Algo.run(C, taxa, characters) -> Result
        // 3) אם isPP: result.tree.writeNewick(...), result.tree.writeSplits(...)
        //    אחרת: הדפס/כתוב witness לפי result.conflict
    }

    private App() {} // no instances
}


