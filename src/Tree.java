import java.util.*;

/** Minimal unrooted tree model + edge labels + Newick writer (SKELETON). */
public class Tree {
    static class Node { int id; String name; Node(int id, String name){ this.id=id; this.name=name; } }
    static class Edge { int u,v; String label; Edge(int u,int v,String label){ this.u=u; this.v=v; this.label=label; } }

    private int nextId = 0;
    private final Map<Integer, Node> nodes = new HashMap<>();
    private final Map<Integer, List<Edge>> adj = new HashMap<>();
    private final Map<Long, List<String>> edgeLabels = new HashMap<>(); // for co-labels

    // Optional: clade-id → node-id mapping if you want Algo to address clades by id
    private final Map<Integer, Integer> cladeToNode = new HashMap<>();

    // --- construction primitives ---
    public int makeInternal(){
        int id = nextId++; nodes.put(id, new Node(id, null)); adj.put(id, new ArrayList<>()); return id;
    }
    public int makeLeaf(String name){
        int id = nextId++; nodes.put(id, new Node(id, name)); adj.put(id, new ArrayList<>()); return id;
    }

    // --- API used by Algo (fill as needed) ---
    public void attachColabelToClade(int cladeId, String label){
        // TODO: decide how to map cladeId→node, and attach label on an existing incident edge
    }
    public void connectClades(int cladeA, int cladeB, String label){
        // TODO: add an undirected edge between the two clade-nodes and store label
    }
    public void connectCladeToLeaf(int cladeId, int leafNode){
        // TODO: connect clade-node to this (already created) leaf
    }

    public void contractDegreeTwoNodes(){
        // TODO: optional simplification – repeatedly contract internal nodes of degree 2
    }

    public String toNewickUnrooted(){
        // TODO: DFS print; pick an arbitrary node as root; sanitize names by replacing spaces/commas
        return "();";
    }

    // --- helpers you may use ---
    private static long key(int a, int b){ if (a>b){int t=a;a=b;b=t;} return (((long)a)<<32) ^ (long)b; }
    private void addUndirectedEdge(int u, int v, String label){
        // TODO: add to adj for both u and v; if label!=null record in edgeLabels
    }
}
