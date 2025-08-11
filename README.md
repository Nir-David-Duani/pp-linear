# Unrooted Perfect Phylogeny (O(nm))

Build an **unrooted** perfect-phylogeny tree from a binary matrix (CSV input).  
If the input is inconsistent, report that no perfect phylogeny exists.

## 📂 Repository layout
```
pp-linear/
  data/      # CSV inputs
  out/       # generated outputs
  src/       # Java source code
    App.java
    CsvIO.java
    Algo.java
    Tree.java
```

## ▶ How to run
1. Place your CSV at `data/input.csv` (first row = reference taxon).
2. Compile:
```
javac src/*.java -d out
```
3. Run:
```
java -cp out App
```

## 📜 Output files
- `out/tree_unrooted.nwk` — unrooted Newick topology  
- `out/splits.csv` — for each character, the induced split  
- Optional: `out/trace.txt`, `out/witness.txt` if no perfect phylogeny exists  

## 🛠 Requirements
- Java 17+  
- UTF-8 encoded files  
