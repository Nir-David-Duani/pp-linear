# pp-linear — Unrooted Perfect Phylogeny (O(nm))

Implementation skeleton for reconstructing an **unrooted perfect phylogeny** from a binary matrix using a linear-time (O(nm)) pipeline:

1. **Normalize by reference taxon** (flip columns where first taxon has 1 → top row becomes all zeros).
2. **Radix sort columns** lexicographically (stable, bottom-up; identical columns become adjacent; supersets follow subsets).
3. **Iterative partition refinement** over clades (Conflict / Co‑label / Split).
4. **Outputs**: All output files are written to the `out` directory, which is created automatically if missing:
    - `out/tree_unrooted.nwk` — Newick string of the unrooted topology (with edge labels)
    - `out/splits.csv` — character-to-clade mapping
    - `out/witness.txt` — conflict report or OK

```
pp-linear/
├─ data/
│   └─ input.csv             # taxon,C1,C2,... (first row = header, first taxon row = reference)
├─ out/                      # created on run
└─ src/
    ├─ App.java              # main entry point
    ├─ CsvIO.java            # CSV I/O and simple helpers
    ├─ Algo.java             # algorithm pipeline 
    └─ Tree.java             # unrooted tree model + Newick + writers 
```

## Build & Run

```powershell
# From the project root:
javac -encoding UTF-8 -source 17 -target 17 -d out src/*.java
java -cp out App
```

By default, the program reads `data/input.csv` and writes all outputs to the `out/` directory.
To run with a different input file, replace `data/input.csv` with your desired file (for example, copy or rename your file to `data/input.csv`), then run the program as usual.

If you want support for specifying the input file name as a command-line argument, let me know and I will add it to both the code and README.

## Input format

- CSV header: first column **taxon**, then character names in the format C1,C2,C3,... (each character must be named exactly C1, C2, C3, ... in order; other names are not allowed)
- Each next row: a taxon name (no commas), followed by `0/1` per character
- The **first taxon row** acts as reference for normalization (flip any column where it has `1`).

**Important:** The input file must use character names in the format C1, C2, C3, ... (no other names are allowed). All outputs will use these names for edge labels and splits.

## Outputs

All output files are written to the `out` directory:

- `tree_unrooted.nwk` — Newick string of the unrooted topology, with edge labels for each character.
- `splits.csv` — each character followed by the clade (set of taxa) it defines; identical columns appear as co‑labels.
- `witness.txt` — `OK` if perfect; otherwise a short explanation with a minimal conflicting pair.

## Notes

- Deterministic: ties resolved by fixed indices/order.
- All‑zero columns are ignored.
- Java 17+, UTF‑8 CSVs.

---
