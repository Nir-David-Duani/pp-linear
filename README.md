# pp-linear — Unrooted Perfect Phylogeny (O(nm))

Implementation skeleton for reconstructing an **unrooted perfect phylogeny** from a binary matrix using a linear-time (O(nm)) pipeline:

1. **Normalize by reference taxon** (flip columns where first taxon has 1 → top row becomes all zeros).
2. **Radix sort columns** lexicographically (stable, bottom-up; identical columns become adjacent; supersets follow subsets).
3. **Iterative partition refinement** over clades (Conflict / Co‑label / Split).
4. **Outputs**: `out/tree_unrooted.nwk`, `out/splits.csv`, `out/witness.txt`.

```
pp-linear/
├─ data/
│   └─ input.csv             # taxon,C1,C2,... (first row = header, first taxon row = reference)
├─ out/                      # created on run
└─ src/
    ├─ App.java              # main entry point
    ├─ CsvIO.java            # CSV I/O and simple helpers
    ├─ Algo.java             # algorithm pipeline (TODOs inside)
    └─ Tree.java             # unrooted tree model + Newick + writers (TODOs inside)
```

## Build & Run

```bash
# from project root
javac -encoding UTF-8 -source 17 -target 17 -d out src/*.java
java -cp out App
```

By default the program reads `data/input.csv` and writes into `out/`.

## Input format

- CSV header: first column **taxon**, then character names (e.g., C1,C2,...)
- Each next row: a taxon name (no commas), followed by `0/1` per character
- The **first taxon row** acts as reference for normalization (flip any column where it has `1`).

## Outputs

- `out/tree_unrooted.nwk` — Newick string of the unrooted topology.
- `out/splits.csv` — each character → the clade (set of taxa) it defines; identical columns appear as co‑labels.
- `out/witness.txt` — `OK` if perfect; otherwise a short explanation with a minimal conflicting pair.

## Notes

- Deterministic: ties resolved by fixed indices/order.
- All‑zero columns are ignored.
- Java 17+, UTF‑8 CSVs.

---
