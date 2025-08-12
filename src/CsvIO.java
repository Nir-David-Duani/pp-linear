import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class CsvIO {

    /** תוצאת הקריאה: שמות טקסונים, שמות תווים, ומטריצה בינארית */
    public static final class ReadResult {
        public final String[] taxa;
        public final String[] characters;
        public final int[][] C;

        public ReadResult(String[] taxa, String[] characters, int[][] C) {
            this.taxa = taxa;
            this.characters = characters;
            this.C = C;
        }
    }

    /** חריגת פורמט CSV (ערכים שאינם 0/1, כותרת חסרה וכו') */
    public static class CsvFormatException extends Exception {
        public CsvFormatException(String message) { super(message); }
        public CsvFormatException(String message, Throwable cause) { super(message, cause); }
    }

    /** אפשרויות קריאה (לא חובה להשתמש בכולן) */
    public static final class Options {
        public final Charset charset;
        public final char delimiter;
        public final boolean dropAllZeroColumns;

        private Options(Charset charset, char delimiter, boolean dropAllZeroColumns) {
            this.charset = charset;
            this.delimiter = delimiter;
            this.dropAllZeroColumns = dropAllZeroColumns;
        }

        public static class Builder {
            private Charset charset = StandardCharsets.UTF_8;
            private char delimiter = ',';
            private boolean dropAllZeroColumns = true;

            public Builder charset(Charset c) { this.charset = c; return this; }
            public Builder delimiter(char d) { this.delimiter = d; return this; }
            public Builder dropAllZeroColumns(boolean b) { this.dropAllZeroColumns = b; return this; }
            public Options build() { return new Options(charset, delimiter, dropAllZeroColumns); }
        }
    }

    /** API נוח ללא Options (משתמש בברירות מחדל) */
    public static ReadResult readCsv(Path csvPath) throws IOException, CsvFormatException {
        // TODO: קרא קובץ באמצעות הספרייה שתבחר (למשל Apache Commons CSV)
        // החזר new ReadResult(taxa, characters, C);
        throw new UnsupportedOperationException("TODO: implement CsvIO.readCsv(Path)");
    }

    /** API עם Options (קידוד, מפריד, סינון עמודות-אפס) */
    public static ReadResult readCsv(Path csvPath, Options options) throws IOException, CsvFormatException {
        // TODO: כמו readCsv הרגיל, רק בהתחשב ב-options
        throw new UnsupportedOperationException("TODO: implement CsvIO.readCsv(Path, Options)");
    }

    private CsvIO() {} // no instances
}
