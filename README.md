# Unrooted Perfect Phylogeny (PP-linear)

A full implementation of the unrooted Perfect Phylogeny algorithm in O(n·m) time (Java).
The system reads a binary matrix from a CSV file, performs **radix sort** on columns (lexicographic order, bottom-to-top), builds an unrooted phylogenetic tree, and produces standard outputs (Newick, splits.csv, witness).

## Project Structure

```
pp-linear/
├─ tests/                    # Test cases and automated testing
│   ├─ test1.csv, test2.csv, ...  # Test input files
│   └─ results/              # Test output directories (created automatically)
│       ├─ test1/            # Results for test1
│       ├─ test2/            # Results for test2
│       └─ ...
└─ src/
    ├─ App.java              # Main entry point
    ├─ TestRunner.java       # Automated test runner
    ├─ CsvIO.java            # Input reading and helpers
    ├─ Algo.java             # Algorithm implementation 
    └─ Tree.java             # Tree building + Newick + splits writers
```

## Usage

### Automated Testing Mode:
1. Compile: `javac src/*.java`
2. Run all tests: `java -cp src TestRunner`
3. Results will appear in `tests/results/test1/`, `tests/results/test2/`, etc.

The project includes 6 pre-configured test cases:
- **test1**: Simple hierarchy (3 taxa, 3 characters)
- **test2**: Two separate branches (4 taxa, 3 characters)  
- **test3**: Three branches (4 taxa, 3 characters)
- **test4**: Complex example (5 taxa, 4 characters)
- **test5**: Hierarchical example (5 taxa, 4 characters)
- **test6**: Conflict example (should fail with witness message)

## Algorithm Details

The implementation uses **true radix sort** for column ordering:
- Sorts columns by each row from **bottom to top** (lexicographic order)
- Maintains stability for deterministic results
- Processes characters from **last to first** column in the sorted matrix
- Creates unrooted phylogenetic tree based on character splits

## Input Format

- Header row: first column is `taxon`, followed by character names (C1,C2,C3,...).
- Each row: taxon name (no commas), followed by 0/1 for each character.

**Important:** Character names must be exactly C1, C2, C3, ... in order.

## Outputs

- `tree_unrooted.nwk` — Phylogenetic tree in Newick format (unrooted).
- `splits.csv` — Splits: character,clade.
- `witness.txt` — "OK" if no conflict, otherwise "NOT A PERFECT PHYLOGENY" with explanation.
- `sorted_matrix.csv` — Matrix after radix sort (for debugging).

## Error Handling

If the input is not compatible with Perfect Phylogeny (conflicts between characters), 
the algorithm will detect this and report the conflicting characters in `witness.txt`.

## Testing

The project includes 6 pre-configured test cases with automated execution:
- **test1**: Simple hierarchy (4 taxa, 3 characters)
- **test2**: Two separate branches (4 taxa, 3 characters)  
- **test3**: Three branches (4 taxa, 3 characters)
- **test4**: Complex example (5 taxa, 4 characters)
- **test5**: Hierarchical example (5 taxa, 4 characters)
- **test6**: Conflict example (5 taxa, 4 characters - should fail)

Execute all tests with: `java -cp src TestRunner`

## Notes
- The algorithm is deterministic (stable radix sort with fixed tie-breaking rules).
- All-zero columns are ignored.
- Requires Java 17+, UTF-8 CSV files.
- Unrooted trees can be rooted at any vertex as needed.

## Technical Implementation

- **Radix Sort**: True lexicographic sorting, row-by-row from bottom to top
- **Tree Building**: Processes characters in reverse order (right to left in sorted matrix)
- **Conflict Detection**: Detects overlapping but non-nested character sets
- **Output Format**: Standard Newick format for unrooted trees
