---
parent: Documentation
nav_order: 9
title: Matrix Response
---

# Matrix Response

The Matrix Response contains one matrix for each specified `metrics` value.
In this matrix the items(rows) correspond to the number of `sources` and the entries(columns) of each item correspond to the number of `destinations`.

Here is a simple example for `sources=2,3` and `destinations=all` with 4 `locations`:

  | Location |    L0    |    L1    |    L2    |    L3    |
  |:--------:|:--------:|:--------:|:--------:|:--------:|
  |    L2    | L2 -> L0 | L2 -> L1 |     0    | L2 -> L3 |
  |    L3    | L3 -> L0 | L3 -> L1 | L3 -> L2 |     0    |
  
The result is `null` if a value can't be determined.
