# Expected Properties for `tests/my_cases.jsonl`

The input contains 14 JSONL requests. Some algorithms can return different but equally valid results. Therefore, MST edge IDs, SCC labels, and topological orders should be checked semantically rather than compared byte-for-byte.

| Case | Task               | Expected result                                                                                                                              |
| ---: | ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------- |
|    1 | DSU                | States: `[[3,2],[2,3],[2,3],[2,3]]`. Repeated union and self-union do not change the partition.                                              |
|    2 | DSU (`n=0`)        | Empty states array.                                                                                                                          |
|    3 | Kruskal            | Four edges of weight 1,000,000,000; cost `4000000000`, one component, four selected edges.                                                   |
|    4 | Prim               | Same graph; cost `4000000000`, one component, four selected edges.                                                                           |
|    5 | Kruskal            | Minimum forest cost `-3`, two components. Select one parallel edge of weight `-5` and the edge of weight `2`; ignore the self-loop.          |
|    6 | Prim               | Same semantic result as case 5; the selected ID among equal parallel edges may differ.                                                       |
|    7 | SCC                | Components `{0,1}`, `{2,3}`, `{4}`. Condensation is a chain between these components. Labels may differ.                                     |
|    8 | SCC (`n=0`)        | Empty component labels and empty condensation.                                                                                               |
|    9 | Topological sort   | Cycle `0 → 1 → 2 → 0`; expect `acyclic=false` and empty order.                                                                               |
|   10 | Topological sort   | Acyclic. Every vertex appears once, and each edge's source precedes its destination. Exact order may differ.                                 |
|   11 | DAG shortest paths | Distances `[0,3,-4,0,null]`; parent edge IDs `[-1,0,2,3,-1]`.                                                                                |
|   12 | DAG shortest paths | A cycle in component `{2,3}` means `acyclic=false` and empty distance and parent arrays, even though the cycle is unreachable from source 0. |
|   13 | DAG shortest paths | Distances `[0,2,null,null]`; source parent `-1`; unreachable vertices have `null` distance and parent `-1`.                                  |
|   14 | Ping               | Expected result `{"status":"ready"}`.                                                                                                        |

## Recorded Actual Output



```jsonl
{"states":[[3,2],[2,3],[2,3],[2,3]]}
{"states":[]}
{"cost":4000000000,"edge_ids":[0,1,2,3],"components":1}
{"cost":4000000000,"edge_ids":[0,1,2,3],"components":1}
{"cost":-3,"edge_ids":[0,2],"components":2}
{"cost":-3,"edge_ids":[0,2],"components":2}
{"component":[0,0,1,1,2],"condensation":[[0,1],[1,2]]}
{"component":[],"condensation":[]}
{"acyclic":false,"order":[]}
{"acyclic":true,"order":[4,1,0,2,3]}
{"acyclic":true,"distance":[0,3,-4,0,null],"parent_edge":[-1,0,2,3,-1]}
{"acyclic":false,"distance":[],"parent_edge":[]}
{"acyclic":true,"distance":[0,2,null,null],"parent_edge":[-1,0,-1,-1]}
{"status":"ready"}
```

## Command



```bash
python tools/run.py --input tests/my_cases.jsonl
```

