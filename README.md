# Unrooted Perfect Phylogeny (PP-linear)

A full implementation of the unrooted Perfect Phylogeny algorithm in O(n·m) time (Java).
The system reads a binary matrix from a CSV file, performs stable radix sort on columns, builds an unrooted phylogenetic tree, and produces standard outputs (Newick, splits.csv, witness).

## Project Structure

```
pp-linear/
├─ data/
│   └─ input.csv             # Input CSV file (taxon,C1,C2,...)
├─ out/                      # Output directory (created automatically)
└─ src/
    ├─ App.java              # Main entry point
    ├─ CsvIO.java            # Input reading and helpers
    ├─ Algo.java             # Algorithm implementation
    └─ Tree.java             # Tree building + Newick + splits writers
```

## Usage

1. Place your input CSV file in the `data/` directory as `input.csv`.
2. Run the program (`App.java`).
3. Outputs will appear in the `out/` directory.

## Input Format

- Header row: first column is `taxon`, followed by character names (C1,C2,C3,...).
- Each row: taxon name (no commas), followed by 0/1 for each character.

**Important:** Character names must be exactly C1, C2, C3, ... in order.

## Outputs

- `tree_unrooted.nwk` — Phylogenetic tree in Newick format.
- `splits.csv` — Splits: character,clade.
- `witness.txt` — "OK" if no conflict, otherwise an explanation.
- `sorted_matrix.csv` — Sorted matrix (for debugging).

## Error Handling

If the input is not compatible with the algorithm (conflict between characters), an explanation will appear in `witness.txt`.

## Notes
- The algorithm is deterministic (fixed order for ties).
- All-zero columns are ignored.
- Requires Java 17+, UTF-8 CSV files.
