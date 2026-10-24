# Assignment 3 — Graph Algorithms for a Campus Festival

## 1. Overview

**Language:** Java

**Test environment:** Java 25.0.1 LTS, 64-bit HotSpot JVM

The project implements six graph algorithms and a route reconstruction helper:

1. Disjoint Set Union (DSU)
2. Kruskal's minimum spanning forest
3. Prim's minimum spanning forest
4. Kosaraju's strongly connected components (SCC)
5. Iterative DFS topological sort
6. DAG shortest paths

The implementation preserves original edge IDs where required. Iterative DFS is used for deep graph traversal to avoid Java call-stack overflow.

Each JSON Lines input request is processed independently. Request-specific arrays and data structures must be initialized for every request so that previous requests cannot affect later results.

---

## 2. Disjoint Set Union (DSU)

### Algorithm Steps

1. Initialize each vertex as a separate set.
2. Use `find` to locate the representative of a set.
3. Apply path compression during `find`.
4. Use union by size when merging two different sets.
5. Update the number of components after a successful merge.
6. Record the component count and largest component size after each operation.

### Invariant

After every operation, the DSU structure represents exactly the partition created by all union operations processed so far. The `components` variable equals the number of disjoint sets. The size stored at each root equals the number of elements in that set.

### Correctness

Initially, each vertex belongs to its own set. When two different sets are merged, their sizes are added and the component count decreases by one. If both vertices already belong to the same set, no merge occurs. Therefore, the DSU maintains the correct partition, component count, and largest component size after every operation.

### Boundary Cases

* `n = 0`: the result contains no states.
* A vertex is merged with itself.
* The same union operation is repeated.
* A single vertex.
* A large number of operations.

### Complexity

Let `n` be the number of vertices and `q` the number of operations.

* **Time:** `O(n + q α(n))` amortised.
* **Space:** `O(n + q)`, including recorded results.

Here, `α(n)` is the inverse Ackermann function, which grows extremely slowly.

---

## 3. Kruskal's Algorithm

### Algorithm Steps

1. Preserve the original ID of every input edge.
2. Sort edges by increasing weight.
3. Initialize DSU with every vertex in a separate component.
4. Process edges in sorted order.
5. If an edge's endpoints belong to different components, select the edge and merge the components.
6. Add the selected edge's weight to the total cost.
7. Continue until all edges have been processed.
8. Return the total cost, selected edge IDs, and number of components.

### Invariant

The selected edges always form a forest without cycles. The DSU partition matches the connected components formed by the selected edges. Every accepted edge connects two previously separate components.

### Correctness

An edge is accepted only if its endpoints belong to different components, so adding it cannot create a cycle. Kruskal processes edges in non-decreasing order of weight. By the cut property of minimum spanning trees, choosing the minimum-weight eligible edge is a safe choice. After all edges are processed, each connected component of the input graph is spanned by a minimum-weight tree. Thus, the result is a minimum spanning forest.

### Boundary Cases

* Empty graph.
* Disconnected graph and isolated vertices.
* Self-loops, which must not be selected.
* Parallel edges and equal weights.
* Negative edge weights.
* Large total weights.

The total cost must use `long` to avoid 32-bit integer overflow.

### Complexity

Let `n` be the number of vertices and `m` the number of edges.

* **Time:** `O(n + m log(m + 1))`.
* **Space:** `O(n + m)`.

Sorting is the main time cost.

---

## 4. Prim's Algorithm

### Algorithm Steps

1. Build an adjacency list for the undirected graph, preserving original edge IDs.
2. Mark all vertices as unvisited.
3. Start a traversal from an unvisited vertex.
4. Add candidate edges from visited vertices to unvisited vertices to a priority queue.
5. Select the minimum-weight candidate.
6. Accept it if it leads to an unvisited vertex.
7. Add the edge's weight and original ID to the result.
8. Continue until the current component is complete.
9. Restart from another unvisited vertex to handle disconnected components.

### Invariant

Within the current connected component, the selected edges form a tree connecting all visited vertices. Each accepted edge connects a visited vertex to a new, unvisited vertex, so no cycle is created. The priority queue stores candidate edges that may extend the current tree.

### Correctness

Every accepted edge adds a new vertex to the current tree, so the selected edges remain acyclic. At each step, Prim chooses the minimum-weight edge crossing the cut between visited and unvisited vertices. By the cut property, this edge is safe for a minimum spanning tree. Restarting the algorithm from every unvisited vertex also handles disconnected components and isolated vertices. Therefore, Prim returns a minimum spanning forest.

### Boundary Cases

* `n = 0`.
* Disconnected graphs.
* Isolated vertices.
* Self-loops.
* Parallel edges.
* Equal or negative weights.
* Large total weights.

Kruskal and Prim must return the same minimum cost and component count for the same input, although their selected edge IDs may differ when multiple minimum forests exist.

### Complexity

Using an adjacency list and a lazy priority queue:

* **Time:** `O(n + m log(m + 1))`.
* **Space:** `O(n + m)`.

---

## 5. Kosaraju's Strongly Connected Components

### Algorithm Steps

1. Build the original directed graph.
2. Build the transpose by reversing every edge.
3. Run iterative DFS on the original graph.
4. Record vertices in finishing order.
5. Process vertices in decreasing finishing order.
6. Run DFS on the transposed graph from each unassigned vertex.
7. Assign a component label to every vertex reached in that traversal.
8. Build the condensation graph using edges between different components.
9. Remove duplicate component-to-component edges.

### Invariant

The first DFS pass records every vertex exactly once in finishing order. During the second pass, all vertices reached by one DFS on the transposed graph belong to the same strongly connected component. Each vertex is assigned to exactly one component.

The condensation graph contains an edge between two different components if the original graph contains an edge between their vertices. Duplicate component edges are stored only once.

### Correctness

Kosaraju uses the finishing order from the first DFS to determine the order in which components are discovered in the transposed graph. Processing vertices in decreasing finishing order ensures that each second-pass traversal identifies exactly one strongly connected component. Every vertex is assigned to a component, and no component is split across multiple traversals. The condensation graph is then constructed from the original edges between different components. Therefore, both the SCC partition and the condensation graph are correct.

### Boundary Cases

* Empty graph.
* Isolated vertices.
* Self-loops.
* Duplicate directed edges.
* Multiple edges between the same pair of components.

Component labels may differ between valid implementations. Correctness should be checked by comparing actual SCC membership and condensation relationships rather than requiring fixed numeric labels.

### Complexity

* **Time:** Expected `O(n + m)` with adjacency lists and hash-based deduplication.
* **Space:** `O(n + m)`.

Iterative DFS avoids stack overflow on deep graphs.

---

## 6. Topological Sort — Iterative DFS

### Algorithm Steps

1. Initialize every vertex as unvisited.
2. Use three states: unvisited, active, and finished.
3. Start DFS from every unvisited vertex.
4. Use an explicit stack instead of recursive calls.
5. Store each stack frame's next-neighbour position.
6. If an edge points to an active vertex, report a cycle.
7. After all outgoing neighbours are processed, mark the vertex finished.
8. Append finished vertices to the finishing order.
9. Reverse the finishing order to obtain a topological order.

### Invariant

An active vertex belongs to the current DFS path. A finished vertex has had all its outgoing neighbours processed. A vertex is added to the finishing order only after all its outgoing edges have been examined.

Each stack frame stores the next-neighbour position, allowing the DFS to resume without skipping unprocessed edges.

### Correctness

An edge to an active vertex connects the current vertex to an ancestor on the DFS path, forming a directed cycle. Therefore, if such an edge is found, a topological ordering does not exist.

If no cycle is found, the graph is a DAG. For every directed edge `u → v`, DFS finishes `v` before `u` when `v` is reached from `u`. Reversing the finishing order places `u` before `v`. Since DFS processes every component, the resulting order includes every vertex and respects every edge.

### Boundary Cases

* Empty graph: `acyclic = true`, `order = []`.
* Self-loop: the graph is cyclic.
* Duplicate edges.
* Disconnected graphs.
* A cycle in any component.
* Very deep directed chains.

If a cycle is detected, the result is `acyclic = false` and an empty order.

### Complexity

* **Time:** `O(n + m)`.
* **Space:** `O(n + m)`.

---

## 7. DAG Shortest Paths

### Algorithm Steps

1. Check the directed graph for cycles.
2. If any component contains a cycle, return `acyclic = false` and empty distance and parent arrays.
3. Otherwise, obtain a topological ordering.
4. Initialize distances as unreachable and set the source distance to `0`.
5. Process vertices in topological order.
6. Skip unreachable vertices.
7. Relax every outgoing edge from each reachable vertex.
8. Store the original edge ID when a shorter distance is found.
9. Return distances and parent edge IDs.

### Invariant

Before a vertex is processed, all its incoming neighbours have already been processed because the vertices are in topological order. Therefore, all possible paths to that vertex through its predecessors have already been considered.

When a reachable vertex is processed, its distance is final. Relaxing its outgoing edges correctly updates the distances of its successors. The parent array stores original edge IDs, not predecessor vertex IDs.

### Correctness

In a DAG, every edge points from an earlier vertex to a later vertex in topological order. When a vertex is processed, all paths reaching it through incoming edges have already been considered. Relaxing all outgoing edges therefore propagates the best distances to later vertices. By induction over the topological order, every reachable vertex receives its shortest distance.

Negative edge weights are allowed because the graph is acyclic. Unreachable vertices retain `null` distances. If any component contains a cycle, the graph is not a DAG, so the required invalid result is returned.

### Boundary Cases

* Source distance is `0`.
* Source parent edge ID is `-1`.
* Unreachable vertices have distance `null` and parent edge ID `-1`.
* Negative edge weights.
* A cycle in a disconnected component.
* Large distances requiring `long` arithmetic.

A distance of `-1` is a valid possible distance and must not be confused with the parent edge ID sentinel.

### Complexity

* **Time:** `O(n + m)`.
* **Space:** `O(n + m)`.

---

## 8. Route Reconstruction

The route reconstruction helper follows parent edge IDs backward from the target. It checks each edge and moves to the edge's starting vertex. The collected vertices are reversed to obtain the route from source to target.

### Invariant

At each step, the current vertex is the endpoint of the parent edge being followed. Following a valid parent edge moves one step closer to the source. The collected edges therefore represent a valid path suffix to the target.

When the source is reached, reversing the collected vertices produces the complete route.

### Correctness

Each parent edge is checked against the current vertex before it is followed. This ensures that reconstruction uses valid edge directions and original edge IDs. If the source is reached, the reversed list is a valid route. If the target is unreachable, the helper returns an empty list. When source and target are the same vertex, the route is `[source]`.

### Recorded Direct Test Cases

| Case                 | Expected route | Edge IDs  |       Total weight |
| -------------------- | -------------- | --------- | -----------------: |
| Reachable `0 → 3`    | `[0,1,2,3]`    | `[0,1,2]` | `3 + (-7) + 4 = 0` |
| Source equals target | `[0]`          | `[]`      |                `0` |
| Unreachable target   | `[]`           | `[]`      |           No route |
| Parallel edges       | `[0,1]`        | `[1]`     |                `2` |

For the parallel-edge case, edge ID 1 has weight 2, which is smaller than the alternative edge's weight of 5.

---

## 9. State Reset and Deep Graphs

Each JSON Lines request must have independent state. Arrays, adjacency lists, stacks, queues, DSU structures, counters, and result lists must be initialized for each request.

### Invariant

At the start of a request, all request-specific structures represent only the current input. During iterative DFS, each stack frame retains its next-neighbour position so that traversal can resume correctly.

### Correctness

Fresh state prevents previous requests from affecting later outputs. Explicit DFS stacks reproduce the required traversal logic without relying on recursive calls. This supports processing multiple requests in one process and traversing deep graphs without Java call-stack overflow.

---

## 10. Custom Test Cases

The recorded `tests/my_cases.jsonl` run produced the following 14 outputs:

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

### Expected Properties

| Case | Algorithm          | Expected property                                                   |
| ---: | ------------------ | ------------------------------------------------------------------- |
|    1 | DSU                | Component states: `[[3,2],[2,3],[2,3],[2,3]]`.                      |
|    2 | DSU                | Empty result for `n = 0`.                                           |
|    3 | Kruskal            | Cost `4,000,000,000`, one component, four selected edges.           |
|    4 | Prim               | Same minimum cost and component count as Kruskal.                   |
|    5 | Kruskal            | Cost `-3`, two components; self-loop is not selected.               |
|    6 | Prim               | Same semantic result as Kruskal.                                    |
|    7 | SCC                | Components `{0,1}`, `{2,3}`, `{4}`; condensation is a chain.        |
|    8 | SCC                | Empty graph gives empty component labels and condensation.          |
|    9 | Topological sort   | Cycle detected; empty order.                                        |
|   10 | Topological sort   | Valid order containing every vertex once and respecting every edge. |
|   11 | DAG shortest paths | Distances `[0,3,-4,0,null]`.                                        |
|   12 | DAG shortest paths | Cycle in another component invalidates the request.                 |
|   13 | DAG shortest paths | Distances `[0,2,null,null]`; unreachable vertices retain `null`.    |
|   14 | Ping               | `{"status":"ready"}`.                                               |

For results that are not unique, correctness should be checked semantically. For example, different minimum spanning forests can have different edge IDs but the same optimal cost and component count.

---

## 11. Test Results

### Public Tests

**Recorded result:** all 36 public tests passed, including a check involving two batches processed in the same process. This supports the request-state reset requirement.

### Large-Graph Stress Tests

The recorded large-graph run produced the following results:

| Test               | Vertices | Edges / operations | Result |   Time |
| ------------------ | -------: | -----------------: | ------ | -----: |
| DSU                |  100,000 | 250,000 operations | PASS   |  57 ms |
| Kruskal            |  100,000 |      250,000 edges | PASS   | 274 ms |
| Prim               |  100,000 |      250,000 edges | PASS   | 328 ms |
| Topological sort   |  100,000 |      250,000 edges | PASS   |  55 ms |
| DAG shortest paths |  100,000 |      250,000 edges | PASS   | 104 ms |
| SCC                |  100,000 |      250,000 edges | PASS   | 136 ms |

The run ended with:

```text
ALL LARGE-GRAPH TESTS PASSED
```

The tests checked large input handling, forest validity, deep traversal, topological ordering, DAG distances, and SCC results.

---

## 12. Kruskal vs Prim Benchmark

### Methodology

Both algorithms were tested on the same generated graph at each size. The graph generator used `Random(42)`. Each graph was connected by adding an edge from every new vertex to an earlier vertex, followed by random edges until the requested edge count was reached.

A smaller graph (`n = 200`, `m = 1000`) was used for warm-up. Each algorithm was timed three times using `System.nanoTime()`. The median of the three measurements was reported.

The measured interval included the direct algorithm call but excluded compilation, JSON parsing, and file I/O. Before timing, both returned forests were validated.

The validation checked that:

* Edge IDs were valid and unique.
* No selected edge was a self-loop.
* The selected edges contained no cycles.
* The number of selected edges equalled `n - components`.
* The sum of selected edge weights matched the reported cost.
* Every connected component of the input graph was spanned.

Both algorithms passed the forest validation at all three sizes.

### Results

| Vertices |   Edges | Kruskal timings (ms)         | Kruskal median (ms) | Prim timings (ms)            | Prim median (ms) |        Cost | Components |
| -------: | ------: | ---------------------------- | ------------------: | ---------------------------- | ---------------: | ----------: | ---------: |
|    1,000 |   5,000 | 3.9474, 2.5557, 2.4930       |              2.5557 | 3.3839, 3.7647, 3.3157       |           3.3839 |    -753,245 |          1 |
|   10,000 |  50,000 | 51.2870, 43.2197, 39.9316    |             43.2197 | 32.8361, 32.8441, 31.0248    |          32.8361 |  -7,597,624 |          1 |
|   50,000 | 250,000 | 131.4146, 123.1396, 110.6654 |            123.1396 | 259.0418, 236.5053, 358.7141 |         259.0418 | -37,988,626 |          1 |

### Observations

Kruskal was faster on the 1,000-vertex and 50,000-vertex graphs in these measurements. Prim was faster on the 10,000-vertex graph.

These results apply to the tested environment and are not a universal ranking. Kruskal sorts all edges, while Prim uses a priority queue to select candidate edges. Performance can also depend on graph representation, heap operations, JVM behaviour, and system load.

The observed timings generally increase with graph size, but three measurements per size are not enough to establish a universal performance trend.



---

## 13. AI Assistance

AI assistance was used to clarify algorithm concepts, review test coverage, and prepare documentation.

---
