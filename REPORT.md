# Project Report: Unrooted Perfect Phylogeny (PP-linear)

## Overview

This project implements a Perfect Phylogeny algorithm with O(n·m) time complexity in Java. The algorithm reads a binary matrix from a CSV file, performs radix sort on columns (lexicographic order, bottom-to-top), builds an unrooted phylogenetic tree, and produces output in standard formats.

## Theory - Perfect Phylogeny Algorithm

The algorithm is based on the classical iterative method for building perfect phylogenetic trees:

1. **Column Sorting**: Radix sort of columns in lexicographic order (bottom-to-top)
2. **Split Identification**: Each character creates a split in the tree
3. **Conflict Detection**: If two characters intersect without clear hierarchy, the data is incompatible with Perfect Phylogeny
4. **Tree Construction**: Building the tree based on identified splits

## Code Architecture

### Main Classes:

- **`Algo.java`**: Main algorithm implementation, including radix sort and tree building
- **`App.java`**: Main entry point for the system
- **`CsvIO.java`**: CSV file reading and writing
- **`Tree.java`**: Tree data structure implementation and Newick format export
- **`TestRunner.java`**: Automated test runner for all test cases

### Radix Sort Algorithm

The algorithm uses true radix sort:
- Column sorting by each row from bottom to top (lexicographic order)
- Maintains stability for deterministic results
- Processes characters from right to left in the sorted matrix

## Detailed Examples Description

### Test 1: Simple Hierarchy
**Input:**
```csv
taxon,C1,C2,C3
A,1,0,0
B,1,1,0
C,1,1,1
```

**Data Description:** 3 taxa (A, B, C) with 3 characters (C1, C2, C3)

**Example Uniqueness:** Demonstrates simple and perfect hierarchy - each character is completely contained within the previous one:
- C1: includes all taxa {A,B,C}
- C2: includes only {B,C}  
- C3: includes only {C}

This represents the **ideal nested structure** where characters form a perfect inclusion hierarchy. It's the textbook example of Perfect Phylogeny where no conflicts can arise. The pattern shows clear evolutionary progression: all organisms share C1, fewer share C2, and only one has C3. This creates a **linear evolutionary path** without any branching complexity.

**Output:**
- **Sorted Matrix:** C3,C2,C1 (lexicographic order)
- **Newick Tree:** `((B,C),A);`
- **Equivalent Newick Forms:** `((B,C),A);` or `(B,(C,A));` - all represent the same unrooted linear hierarchy
- **Newick Interpretation:** This unrooted tree is equivalent to the linear hierarchy A-{B,C}-{C}, representing the nested character progression where A is ancestral, B and C share an intermediate state, and C has the most derived state.
- **Splits:** C1→{A,B,C}, C2→{B,C}, C3→{C}
- **Result:** OK (perfect phylogeny)

**What the Example Demonstrates:** The basic case of Perfect Phylogeny - clear hierarchy without conflicts.

---

### Test 2: Two Separate Branches
**Input:**
```csv
taxon,C1,C2,C3,C4
A,0,1,1,0
B,0,1,1,0
C,0,1,0,0
D,0,0,0,1
E,1,0,0,1
```

**Data Description:** 5 taxa with 4 characters

**Example Uniqueness:** Demonstrates a tree with two separate main branches:
- Branch 1: {A,B,C} with shared characters C2,C3
- Branch 2: {D,E} with shared character C4
- Character C1 appears only in E

This example showcases **independent evolutionary lineages** - two groups that evolved separately from a common ancestor. The uniqueness lies in having **disjoint character sets** between the main branches (C2,C3 vs C4), while C1 creates an additional split within one branch. This tests the algorithm's ability to handle **multiple independent clades** and demonstrates how different evolutionary pressures can create separate lineages with distinct trait patterns.

**Output:**
- **Sorted Matrix:** C1,C4,C3,C2
- **Newick Tree:** `((C,(A,B)),(D,E));`
- **Equivalent Newick Forms:** `((C,(A,B)),(D,E));` or `(C,((A,B),(D,E)));` - all represent the same unrooted two-branch topology
- **Newick Interpretation:** This unrooted tree represents two major evolutionary lineages: the (C,(A,B)) clade sharing C2 and C3 characters, and the (D,E) clade sharing C4, with E having additional character C1. The unrooted topology correctly shows the two independent evolutionary branches.
- **Splits:** C2→{A,B,C}, C3→{A,B}, C4→{D,E}, C1→{E}
- **Result:** OK

**What the Example Demonstrates:** Algorithm's ability to handle more complex tree structure with multiple main branches.

---

### Test 3: Three Independent Characters
**Input:**
```csv
taxon,C1,C2,C3
A,1,0,0
B,0,1,0
C,0,0,1
```

**Data Description:** 3 taxa with 3 characters

**Example Uniqueness:** Structure with completely independent character distributions:
- A: only with C1
- B: only with C2
- C: only with C3

This example demonstrates the **most independent case possible** where each character defines a unique taxon with no overlap between any characters. Unlike hierarchical patterns or shared traits, here we have **completely disjoint character sets** where each evolutionary event is entirely separate. This represents a **perfect polytomy scenario** where three lineages diverged simultaneously from a common ancestor, each acquiring a distinct trait. The uniqueness lies in having **zero character overlap**, which tests the algorithm's ability to handle **maximal independence** and shows how completely separate evolutionary events can be represented in a phylogenetic tree.

**Output:**
- **Sorted Matrix:** C3,C2,C1 (lexicographic order)
- **Newick Tree:** `((C,B),A);`
- **Equivalent Newick Forms:** `(A,(C,B));` or `(B,(C,A));` or ideally `(A,B,C);` (polytomy) - all represent the same unrooted three-way split
- **Newick Interpretation:** This unrooted tree represents a perfect polytomy where all three taxa (A, B, C) diverged simultaneously from a common ancestor, each acquiring a unique character. In an ideal unrooted representation, this would be shown as three branches meeting at a central point: A-●-B-●-C, which is equivalent to the binary representation shown.
- **Splits:** C1→{A}, C2→{B}, C3→{C}
- **Result:** OK

**What the Example Demonstrates:** Algorithm's ability to handle completely independent character distributions representing perfect polytomy scenarios.

---

### Test 4: Complex Example
**Input:**
```csv
taxon,C1,C2,C3,C4
A,1,0,0,0
B,1,0,0,0
C,1,0,0,0
D,0,0,1,0
E,0,0,1,1
```

**Data Description:** 5 taxa with 4 characters

**Example Uniqueness:** Demonstrates complex case with:
- Large group {A,B,C} with shared character C1
- Small group {D,E} with shared character C3
- E with additional character C4

This example highlights **asymmetric evolution** where different lineages have undergone different amounts of change. The uniqueness lies in the **unequal group sizes** and **differential character acquisition**. Group {A,B,C} represents a conserved lineage with minimal change (only C1), while {D,E} shows more evolutionary activity with multiple character acquisitions (C3, C4). This tests the algorithm's robustness with **unbalanced tree structures** and demonstrates real-world scenarios where some species evolve faster than others.

**Output:**
- **Newick Tree:** `((A,B,C),(D,E));`
- **Equivalent Newick Forms:** `((A,B,C),(D,E));` or `(A,((B,C),(D,E)));` - all represent the same unrooted two-branch topology
- **Splits:** C1→{A,B,C}, C3→{D,E}, C4→{E}
- **Result:** OK
- **Newick Interpretation:** The unrooted tree shows two distinct evolutionary lineages: a larger clade {A,B,C} sharing character C1, and a smaller clade {D,E} where both share C3, with E having additional character C4. This represents asymmetric evolution with different rates of character acquisition in each lineage.

**What the Example Demonstrates:** Handling more complex data structures with groups of different sizes.

---

### Test 5: Hierarchical Example
**Input:**
```csv
taxon,C1,C2,C3
A,1,0,0
B,1,0,0
C,0,1,0
D,0,1,1
E,0,1,1
```

**Data Description:** 5 taxa with 3 characters

**Example Uniqueness:** Clear hierarchical structure:
- {A,B}: group with C1
- {C,D,E}: group with C2
- {D,E}: sub-group with additional C3

This example demonstrates **nested hierarchical evolution** with multiple levels of character acquisition. The uniqueness is in the **perfect nesting pattern**: C1 defines one clade, C2 defines a separate clade, and C3 creates a sub-clade within the C2 group. This represents **sequential evolutionary events** where characters are gained in a specific order, creating a multi-tiered phylogenetic structure. It tests the algorithm's ability to handle **deep hierarchies** and shows how gradual character accumulation creates nested evolutionary relationships.

**Output:**
- **Newick Tree:** `(A,B,(C,(D,E)));`
- **Equivalent Newick Forms:** `((A,B),(C,(D,E)));` or `(C,((D,E),(A,B)));` - all represent the same unrooted hierarchical topology
- **Splits:** C1→{A,B}, C2→{C,D,E}, C3→{D,E}
- **Result:** OK
- **Newick Interpretation:** The unrooted tree represents a multi-level hierarchical structure where {A,B} form one evolutionary branch with character C1, while {C,D,E} form another branch with character C2, and within this second branch, {D,E} form a sub-clade with additional character C3. This shows nested evolutionary relationships with sequential character acquisition.

**What the Example Demonstrates:** Algorithm's ability to handle multi-level hierarchies.

---

### Test 6: Conflict Example
**Input:**
```csv
taxon,C1,C2,C3,C4
A,1,0,1,0
B,0,1,0,1
C,1,1,0,0
D,0,0,1,1
E,1,0,0,1
```

**Data Description:** 5 taxa with 4 characters

**Example Uniqueness:** **Problematic case** - data is incompatible with Perfect Phylogeny:
- Characters C1, C2, C3, C4 create complex overlaps
- Cannot build a perfect phylogenetic tree from this data

This example is critically important because it demonstrates **character conflict** - a situation where the evolutionary relationships cannot be represented as a perfect tree. The uniqueness lies in having **incompatible character distributions**: multiple characters overlap in ways that contradict tree-like evolution. For example, if we examine the character patterns, we find cases where characters appear to have been gained and lost multiple times, or where different characters suggest conflicting evolutionary relationships. This tests the algorithm's **error detection capabilities** and demonstrates real-world scenarios where data doesn't fit the Perfect Phylogeny model due to phenomena like **convergent evolution**, **horizontal gene transfer**, or **character reversal**.

**Output:**
- **Sorted Matrix:** C4,C2,C3,C1
- **Result:** `NOT A PERFECT PHYLOGENY - conflict: C3`
- **No Tree:** Algorithm detected conflict and did not build a tree
- **Conflict Explanation:** The character distributions create incompatible splits that cannot be resolved into a single tree topology, indicating phenomena like convergent evolution or horizontal gene transfer that violate the Perfect Phylogeny assumption.

**What the Example Demonstrates:** Conflict detection capability - algorithm identifies when data is incompatible with Perfect Phylogeny and reports the problematic character.

## Technical Summary

### Implementation Advantages:
1. **Time Efficiency:** O(n·m) - optimal for this problem
2. **Determinism:** Consistent results due to stable sorting
3. **Error Detection:** Conflict detection with accurate reporting
4. **Standard Formats:** Output in standard Newick format

**Note on Newick Format:** Unrooted trees can be represented in multiple equivalent ways in Newick format. For example, the tree `((B,C),A);` could also be written as `(A,(B,C));` or `(B,(C,A));` - all represent the same unrooted topology. The algorithm produces one consistent representation, but the biological meaning remains identical regardless of the specific Newick string format used.

### Output Files:
- **`tree_unrooted.nwk`**: Phylogenetic tree in Newick format
- **`splits.csv`**: List of splits by characters
- **`witness.txt`**: Result status (OK or conflict description)
- **`sorted_matrix.csv`**: Sorted matrix for debugging purposes

### Use Cases:
The project is suitable for phylogenetic analysis of binary data (presence/absence of traits) in biological, evolutionary, or computational research.