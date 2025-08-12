public final class Algo {

    /** תיאור קונפליקט מינימלי (לא חובה כל שדות — זה רק שלד) */
    public static final class Conflict {
        public final String message;
        public final Integer colA; // אינדקס עמודה א'
        public final Integer colB; // אינדקס עמודה ב'
        public Conflict(String message, Integer colA, Integer colB) {
            this.message = message;
            this.colA = colA;
            this.colB = colB;
        }
    }

    /** תוצאת האלגוריתם: עץ אם יש PP, אחרת קונפליקט */
    public static final class Result {
        public final Tree tree;        // null אם אין עץ
        public final boolean isPP;     // true אם יש Perfect Phylogeny
        public final Conflict conflict; // null אם אין קונפליקט
        public Result(Tree tree, boolean isPP, Conflict conflict) {
            this.tree = tree;
            this.isPP = isPP;
            this.conflict = conflict;
        }
    }

    /**
     * נקודת הכניסה לאלגוריתם O(nm):
     * 1) normalize
     * 2) radix sort
     * 3) partition refinement → בניית Tree או זיהוי Conflict
     */
    public static Result run(int[][] C, String[] taxa, String[] characters) {
        // TODO: לממש את שלבי האלגוריתם
        // החזר new Result(tree, true, null) או new Result(null, false, conflict)
        throw new UnsupportedOperationException("TODO: implement Algo.run");
    }

    private Algo() {} // no instances
}

