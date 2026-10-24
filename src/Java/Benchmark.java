import java.util.*;

public class Benchmark {
    private static final Random random = new Random(42);

    public static void main(String[] args) {
        int[][] sizes = {
                {1000, 5000},
                {10000, 50000},
                {50000, 250000}
        };

        Map<String, Object> warmup = createRequest(200, 1000);
        Algorithms.kruskal(warmup);
        Algorithms.prim(warmup);

        for (int[] size : sizes) {
            int n = size[0];
            int m = size[1];
            Map<String, Object> request = createRequest(n, m);


            Map<String, Object> kruskalResult = Algorithms.kruskal(request);
            Map<String, Object> primResult = Algorithms.prim(request);
            validateForest(request, kruskalResult, "Kruskal");
            validateForest(request, primResult, "Prim");

            long kruskalCost = ((Number) kruskalResult.get("cost")).longValue();
            long primCost = ((Number) primResult.get("cost")).longValue();
            int kruskalComponents = ((Number) kruskalResult.get("components")).intValue();
            int primComponents = ((Number) primResult.get("components")).intValue();

            if (kruskalCost != primCost || kruskalComponents != primComponents) {
                throw new AssertionError("Kruskal and Prim results do not match");
            }

            System.out.println();
            System.out.println("n = " + n + ", m = " + m);
            System.out.println("Both forest witnesses passed validation.");
            System.out.println("Cost = " + kruskalCost);
            System.out.println("Components = " + kruskalComponents);

            long[] kruskalTimes = new long[3];
            long[] primTimes = new long[3];

            for (int i = 0; i < 3; i++) {
                long start = System.nanoTime();
                Algorithms.kruskal(request);
                kruskalTimes[i] = System.nanoTime() - start;

                start = System.nanoTime();
                Algorithms.prim(request);
                primTimes[i] = System.nanoTime() - start;
            }

            printTimes("Kruskal", kruskalTimes);
            printTimes("Prim", primTimes);
        }
    }

    private static void validateForest(Map<String, Object> request,
                                       Map<String, Object> result,
                                       String algorithm) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Integer>> edges = (List<List<Integer>>) request.get("edges");
        List<?> selectedIds = (List<?>) result.get("edge_ids");
        long reportedCost = ((Number) result.get("cost")).longValue();
        int reportedComponents = ((Number) result.get("components")).intValue();

        DSU inputDsu = new DSU(n);
        for (List<Integer> edge : edges) {
            inputDsu.union(edge.get(0), edge.get(1));
        }
        int inputComponents = inputDsu.components;

        DSU forestDsu = new DSU(n);
        Set<Integer> uniqueIds = new HashSet<>();
        long selectedCost = 0L;

        for (Object rawId : selectedIds) {
            if (!(rawId instanceof Number)) {
                throw new AssertionError(algorithm + ": edge ID is not numeric");
            }
            int id = ((Number) rawId).intValue();
            if (id < 0 || id >= edges.size()) {
                throw new AssertionError(algorithm + ": invalid edge ID " + id);
            }
            if (!uniqueIds.add(id)) {
                throw new AssertionError(algorithm + ": duplicate edge ID " + id);
            }

            List<Integer> edge = edges.get(id);
            int u = edge.get(0);
            int v = edge.get(1);
            int weight = edge.get(2);

            if (u == v) {
                throw new AssertionError(algorithm + ": selected self-loop " + id);
            }
            if (!forestDsu.union(u, v)) {
                throw new AssertionError(algorithm + ": selected edges contain a cycle");
            }
            selectedCost += weight;
        }

        if (selectedIds.size() != n - reportedComponents) {
            throw new AssertionError(algorithm + ": edge count is not n - components");
        }
        if (selectedCost != reportedCost) {
            throw new AssertionError(algorithm + ": selected edge sum differs from cost");
        }
        if (reportedComponents != inputComponents) {
            throw new AssertionError(algorithm + ": wrong number of input components");
        }
        if (forestDsu.components != reportedComponents) {
            throw new AssertionError(algorithm + ": selected forest component count mismatch");
        }

        // Every input edge's endpoints must remain connected in the selected forest.
        // This proves each connected component of the input graph is spanned.
        for (List<Integer> edge : edges) {
            int u = edge.get(0);
            int v = edge.get(1);
            if (inputDsu.find(u) == inputDsu.find(v)
                    && forestDsu.find(u) != forestDsu.find(v)) {
                throw new AssertionError(algorithm + ": an input component is not spanned");
            }
        }
    }

    private static Map<String, Object> createRequest(int n, int m) {
        List<List<Integer>> edges = new ArrayList<>();

        for (int v = 1; v < n && edges.size() < m; v++) {
            int parent = random.nextInt(v);
            int weight = random.nextInt(2001) - 1000;
            edges.add(Arrays.asList(parent, v, weight));
        }

        while (edges.size() < m) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            int weight = random.nextInt(2001) - 1000;
            edges.add(Arrays.asList(u, v, weight));
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("task", "kruskal");
        request.put("n", n);
        request.put("edges", edges);
        return request;
    }

    private static void printTimes(String name, long[] times) {
        double[] milliseconds = new double[times.length];
        for (int i = 0; i < times.length; i++) {
            milliseconds[i] = times[i] / 1_000_000.0;
        }
        System.out.println(name + " times (ms): " + Arrays.toString(milliseconds));
        System.out.println(name + " median (ms): " + median(milliseconds));
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    private static class DSU {
        private final int[] parent;
        private final int[] size;
        private int components;

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
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) return false;
            if (size[rootA] < size[rootB]) {
                int temp = rootA;
                rootA = rootB;
                rootB = temp;
            }
            parent[rootB] = rootA;
            size[rootA] += size[rootB];
            components--;
            return true;
        }
    }
}
