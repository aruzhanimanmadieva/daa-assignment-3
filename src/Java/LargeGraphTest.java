import java.util.*;

public class LargeGraphTest {
    private static final int N = 100_000;
    private static final int M = 250_000;
    private static final int Q = 250_000;

    public static void main(String[] args) {
        testDsuMaximum();
        testUndirectedMaximum();
        testDeepDirectedDag();
        testDeepSccGraph();
        System.out.println("ALL LARGE-GRAPH TESTS PASSED");
    }

    private static void testDsuMaximum() {
        List<List<Integer>> operations = new ArrayList<>(Q);
        for (int i = 0; i < N - 1; i++) {
            operations.add(Arrays.asList(i, i + 1));
        }
        for (int i = N - 1; i < Q; i++) {
            int u = (i * 31) % N;
            int v = (i * 97 + 17) % N;
            operations.add(Arrays.asList(u, v));
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("task", "dsu");
        request.put("n", N);
        request.put("operations", operations);

        long start = System.nanoTime();
        Map<String, Object> result = Algorithms.dsu(request);
        long ms = (System.nanoTime() - start) / 1_000_000;
        List<?> states = (List<?>) result.get("states");
        require(states != null && states.size() == Q, "DSU must return one state per operation");
        int[] last = pair(states.get(states.size() - 1));
        require(last[0] == 1 && last[1] == N, "DSU final state should be one component of size n");
        System.out.println("DSU n=" + N + ", q=" + Q + " passed (" + ms + " ms)");
    }

    private static void testUndirectedMaximum() {
        List<List<Integer>> edges = new ArrayList<>(M);
        Random random = new Random(42);

        // A spanning chain ensures the graph is connected.
        for (int v = 1; v < N; v++) {
            edges.add(Arrays.asList(v - 1, v, random.nextInt(2001) - 1000));
        }
        // Add unique non-loop edges until m = 250,000.
        Set<Long> used = new HashSet<>(M * 2);
        for (List<Integer> e : edges) used.add(pairKey(e.get(0), e.get(1)));
        while (edges.size() < M) {
            int u = random.nextInt(N);
            int v = random.nextInt(N);
            if (u == v) continue;
            long key = pairKey(u, v);
            if (!used.add(key)) continue;
            edges.add(Arrays.asList(u, v, random.nextInt(2001) - 1000));
        }

        Map<String, Object> request = request("kruskal", N, edges);
        long start = System.nanoTime();
        Map<String, Object> kruskal = Algorithms.kruskal(request);
        long kruskalMs = (System.nanoTime() - start) / 1_000_000;
        validateForest(request, kruskal, "Kruskal");

        request.put("task", "prim");
        start = System.nanoTime();
        Map<String, Object> prim = Algorithms.prim(request);
        long primMs = (System.nanoTime() - start) / 1_000_000;
        validateForest(request, prim, "Prim");

        require(((Number) kruskal.get("cost")).longValue()
                        == ((Number) prim.get("cost")).longValue(),
                "Kruskal and Prim costs differ");
        require(((Number) kruskal.get("components")).intValue()
                        == ((Number) prim.get("components")).intValue(),
                "Kruskal and Prim component counts differ");
        System.out.println("MST n=" + N + ", m=" + M
                + " passed (Kruskal " + kruskalMs + " ms, Prim " + primMs + " ms)");
    }

    private static void testDeepDirectedDag() {
        List<List<Integer>> edges = new ArrayList<>(M);
        // Deep chain catches recursive DFS implementations.
        for (int v = 0; v < N - 1; v++) {
            edges.add(Arrays.asList(v, v + 1, 1));
        }
        // Additional forward edges keep the graph acyclic.
        Random random = new Random(43);
        Set<Long> used = new HashSet<>(M * 2);
        for (List<Integer> e : edges) used.add(directedKey(e.get(0), e.get(1)));
        while (edges.size() < M) {
            int u = random.nextInt(N - 1);
            int v = u + 1 + random.nextInt(N - u - 1);
            long key = directedKey(u, v);
            if (!used.add(key)) continue;
            edges.add(Arrays.asList(u, v, v - u));
        }

        Map<String, Object> topoRequest = request("topo", N, edges);
        long start = System.nanoTime();
        Map<String, Object> topo = Algorithms.topo(topoRequest);
        long topoMs = (System.nanoTime() - start) / 1_000_000;
        require(Boolean.TRUE.equals(topo.get("acyclic")), "Deep DAG should be acyclic");
        List<?> order = (List<?>) topo.get("order");
        require(order != null && order.size() == N, "Topological order should contain all vertices");
        int[] position = new int[N];
        for (int i = 0; i < order.size(); i++) position[((Number) order.get(i)).intValue()] = i;
        for (List<Integer> e : edges) {
            require(position[e.get(0)] < position[e.get(1)], "Invalid topological edge order");
        }

        Map<String, Object> dagRequest = new LinkedHashMap<>(topoRequest);
        dagRequest.put("task", "dag");
        dagRequest.put("source", 0);
        start = System.nanoTime();
        Map<String, Object> dag = Algorithms.dag(dagRequest);
        long dagMs = (System.nanoTime() - start) / 1_000_000;
        require(Boolean.TRUE.equals(dag.get("acyclic")), "Deep DAG shortest paths should be acyclic");
        List<?> distances = (List<?>) dag.get("distance");
        require(distances != null && distances.size() == N, "DAG distances should cover all vertices");
        for (int i = 0; i < N; i++) {
            require(distances.get(i) instanceof Number, "All vertices should be reachable in the chain");
            require(((Number) distances.get(i)).longValue() == i, "Unexpected shortest distance at vertex " + i);
        }
        System.out.println("Deep DAG n=" + N + ", m=" + M
                + " passed (topo " + topoMs + " ms, DAG " + dagMs + " ms)");
    }

    private static void testDeepSccGraph() {
        List<List<Integer>> edges = new ArrayList<>(M);
        for (int v = 0; v < N - 1; v++) edges.add(Arrays.asList(v, v + 1));
        Random random = new Random(44);
        Set<Long> used = new HashSet<>(M * 2);
        for (List<Integer> e : edges) used.add(directedKey(e.get(0), e.get(1)));
        while (edges.size() < M) {
            int u = random.nextInt(N - 1);
            int v = u + 1 + random.nextInt(N - u - 1);
            long key = directedKey(u, v);
            if (!used.add(key)) continue;
            edges.add(Arrays.asList(u, v));
        }

        Map<String, Object> request = request("scc", N, edges);
        long start = System.nanoTime();
        Map<String, Object> result = Algorithms.scc(request);
        long ms = (System.nanoTime() - start) / 1_000_000;
        List<?> components = (List<?>) result.get("component");
        require(components != null && components.size() == N, "SCC result must label every vertex");
        Set<Integer> unique = new HashSet<>();
        for (Object value : components) unique.add(((Number) value).intValue());
        require(unique.size() == N, "A directed acyclic graph should have n singleton SCCs");
        System.out.println("Deep SCC graph n=" + N + ", m=" + M + " passed (" + ms + " ms)");
    }

    private static Map<String, Object> request(String task, int n, List<List<Integer>> edges) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("task", task);
        request.put("n", n);
        request.put("edges", edges);
        return request;
    }

    private static void validateForest(Map<String, Object> request, Map<String, Object> result, String name) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Integer>> edges = (List<List<Integer>>) request.get("edges");
        List<?> ids = (List<?>) result.get("edge_ids");
        long cost = ((Number) result.get("cost")).longValue();
        int components = ((Number) result.get("components")).intValue();

        DSU input = new DSU(n);
        for (List<Integer> edge : edges) input.union(edge.get(0), edge.get(1));

        DSU forest = new DSU(n);
        Set<Integer> uniqueIds = new HashSet<>();
        long sum = 0;
        for (Object raw : ids) {
            int id = ((Number) raw).intValue();
            require(id >= 0 && id < edges.size(), name + " invalid edge ID");
            require(uniqueIds.add(id), name + " duplicate edge ID");
            List<Integer> edge = edges.get(id);
            require(!edge.get(0).equals(edge.get(1)), name + " selected a loop");
            require(forest.union(edge.get(0), edge.get(1)), name + " selected a cycle");
            sum += edge.get(2);
        }
        require(ids.size() == n - components, name + " wrong edge count");
        require(sum == cost, name + " cost differs from selected edge weights");
        require(components == input.components, name + " wrong input component count");
        require(forest.components == components, name + " forest does not span input components");
    }

    private static int[] pair(Object value) {
        List<?> list = (List<?>) value;
        return new int[] { ((Number) list.get(0)).intValue(), ((Number) list.get(1)).intValue() };
    }

    private static long pairKey(int u, int v) {
        int a = Math.min(u, v), b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    private static long directedKey(int u, int v) {
        return (((long) u) << 32) | (v & 0xffffffffL);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class DSU {
        final int[] parent;
        final int[] size;
        int components;

        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            components = n;
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]];
                x = parent[x];
            }
            return x;
        }

        boolean union(int a, int b) {
            a = find(a); b = find(b);
            if (a == b) return false;
            if (size[a] < size[b]) { int t = a; a = b; b = t; }
            parent[b] = a;
            size[a] += size[b];
            components--;
            return true;
        }
    }
}
