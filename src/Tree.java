import java.nio.file.Path;

public final class Tree {

    /** ייצוג צומת (שלד בלבד) */
    public static final class Node {
        public final int id;
        public final boolean isLeaf;
        public final Integer taxonIndex; // null אם פנימי
        public Node(int id, boolean isLeaf, Integer taxonIndex) {
            this.id = id;
            this.isLeaf = isLeaf;
            this.taxonIndex = taxonIndex;
        }
    }

    /** ייצוג קשת (שלד בלבד) */
    public static final class Edge {
        public final int u;
        public final int v;
        // TODO: שמור כאן רשימת תווים (co-labels) אם תרצה
        public Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    // TODO: אחסן כאן מבני נתונים של nodes/edges לפי העדפה שלך

    /** הוספת קשת חדשה עם תו אחד (שלד) */
    public int addEdge(int nodeA, int nodeB, int characterIndex) {
        // TODO: צור קשת, שמור characterIndex כתווית, והחזר מזהה קשת
        throw new UnsupportedOperationException("TODO: implement Tree.addEdge");
    }

    /** הוספת תו נוסף כ-co-label על קשת קיימת (שלד) */
    public void addCoLabel(int edgeId, int characterIndex) {
        // TODO: הוסף characterIndex לרשימת התוויות של הקשת
        throw new UnsupportedOperationException("TODO: implement Tree.addCoLabel");
    }

    /** אם נוצר root בדרגה 2, אפשר להתיך אותו (כדי להשאיר עץ לא-מכוון נקי) */
    public void contractDegree2Root() {
        // TODO
        throw new UnsupportedOperationException("TODO: implement Tree.contractDegree2Root");
    }

    /** כתיבת קובץ Newick */
    public void writeNewick(Path outFile) {
        // TODO: סדר את המבנה וכתוב Newick לא מכוון
        throw new UnsupportedOperationException("TODO: implement Tree.writeNewick");
    }

    /** כתיבת splits.csv */
    public void writeSplits(Path outFile, String[] characters) {
        // TODO: עבור כל תו, כתוב את הפיצול שהוא משרה; נהל גם co-labels
        throw new UnsupportedOperationException("TODO: implement Tree.writeSplits");
    }
}
