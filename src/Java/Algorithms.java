
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class Algorithms {

    // -------------------- DSU --------------------

    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        int largest = n == 0 ? 0 : 1;

        List<List<Integer>> states = new ArrayList<>();

        for (int[] operation : operations) {
            int u = find(parent, operation[0]);
            int v = find(parent, operation[1]);

            if (u != v) {
                if (size[u] < size[v]) {
                    int temp = u;
                    u = v;
                    v = temp;
                }

                parent[v] = u;
                size[u] += size[v];

                components--;
                largest = Math.max(largest, size[u]);
            }

            states.add(Arrays.asList(components, largest));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("states", states);

        return result;
    }

    private static int find(int[] parent, int vertex) {
        while (parent[vertex] != vertex) {
            parent[vertex] = parent[parent[vertex]];
            vertex = parent[vertex];
        }

        return vertex;
    }

    // -------------------- KRUSKAL --------------------

    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);
        int m = edges.length;

        Integer[] edgeIds = new Integer[m];

        for (int i = 0; i < m; i++) {
            edgeIds[i] = i;
        }

        Arrays.sort(edgeIds, Comparator
                .comparingInt((Integer id) -> edges[id][2])
                .thenComparingInt(id -> id));

        int[] parent = new int[n];
        int[] size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        long cost = 0;

        List<Integer> selectedIds = new ArrayList<>();

        for (int id : edgeIds) {
            int u = edges[id][0];
            int v = edges[id][1];
            int weight = edges[id][2];

            int rootU = find(parent, u);
            int rootV = find(parent, v);

            if (rootU == rootV) {
                continue;
            }

            if (size[rootU] < size[rootV]) {
                int temp = rootU;
                rootU = rootV;
                rootV = temp;
            }

            parent[rootV] = rootU;
            size[rootU] += size[rootV];

            cost += weight;
            selectedIds.add(id);
            components--;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", selectedIds);
        result.put("components", components);

        return result;
    }

    // -------------------- PRIM --------------------

    private static final class WeightedEdge {
        int from;
        int to;
        int weight;
        int id;

        WeightedEdge(int from, int to, int weight, int id) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.id = id;
        }
    }

    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<WeightedEdge>> graph = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        for (int id = 0; id < edges.length; id++) {
            int u = edges[id][0];
            int v = edges[id][1];
            int weight = edges[id][2];

            graph.get(u).add(new WeightedEdge(u, v, weight, id));
            graph.get(v).add(new WeightedEdge(v, u, weight, id));
        }

        Comparator<WeightedEdge> comparator = Comparator
                .comparingInt((WeightedEdge edge) -> edge.weight)
                .thenComparingInt(edge -> edge.id);

        PriorityQueue<WeightedEdge> heap =
                new PriorityQueue<>(comparator);

        boolean[] visited = new boolean[n];

        int components = 0;
        long cost = 0;

        List<Integer> selectedIds = new ArrayList<>();

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            components++;
            visited[start] = true;

            for (WeightedEdge edge : graph.get(start)) {
                if (!visited[edge.to]) {
                    heap.add(edge);
                }
            }

            while (!heap.isEmpty()) {
                WeightedEdge edge = heap.poll();

                if (visited[edge.to]) {
                    continue;
                }

                visited[edge.to] = true;

                cost += edge.weight;
                selectedIds.add(edge.id);

                for (WeightedEdge next : graph.get(edge.to)) {
                    if (!visited[next.to]) {
                        heap.add(next);
                    }
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", selectedIds);
        result.put("components", components);

        return result;
    }

    // -------------------- SCC / KOSARAJU --------------------

    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> graph = new ArrayList<>();
        List<List<Integer>> transpose = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
            transpose.add(new ArrayList<>());
        }

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];

            graph.get(u).add(v);
            transpose.get(v).add(u);
        }

        boolean[] visited = new boolean[n];
        int[] nextIndex = new int[n];
        int[] stack = new int[n];

        List<Integer> finishOrder = new ArrayList<>(n);

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            int top = 0;
            stack[top++] = start;
            visited[start] = true;

            while (top > 0) {
                int vertex = stack[top - 1];

                if (nextIndex[vertex] < graph.get(vertex).size()) {
                    int next = graph.get(vertex).get(nextIndex[vertex]++);

                    if (!visited[next]) {
                        visited[next] = true;
                        stack[top++] = next;
                    }
                } else {
                    finishOrder.add(vertex);
                    top--;
                }
            }
        }

        Arrays.fill(visited, false);

        int[] component = new int[n];
        Arrays.fill(component, -1);

        int componentCount = 0;

        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int start = finishOrder.get(i);

            if (visited[start]) {
                continue;
            }

            int top = 0;
            stack[top++] = start;
            visited[start] = true;
            component[start] = componentCount;

            while (top > 0) {
                int vertex = stack[--top];

                for (int next : transpose.get(vertex)) {
                    if (!visited[next]) {
                        visited[next] = true;
                        component[next] = componentCount;
                        stack[top++] = next;
                    }
                }
            }

            componentCount++;
        }

        Set<Long> uniquePairs = new HashSet<>();

        for (int[] edge : edges) {
            int from = component[edge[0]];
            int to = component[edge[1]];

            if (from != to) {
                long key = ((long) from << 32)
                        | (to & 0xffffffffL);

                uniquePairs.add(key);
            }
        }

        List<List<Integer>> condensation = new ArrayList<>();

        for (long key : uniquePairs) {
            int from = (int) (key >> 32);
            int to = (int) key;

            condensation.add(Arrays.asList(from, to));
        }

        List<Integer> labels = new ArrayList<>(n);

        for (int label : component) {
            labels.add(label);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", labels);
        result.put("condensation", condensation);

        return result;
    }

    // -------------------- TOPOLOGICAL SORT --------------------

    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> graph = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        for (int[] edge : edges) {
            graph.get(edge[0]).add(edge[1]);
        }

        int[] order = topologicalOrder(graph);

        Map<String, Object> result = new LinkedHashMap<>();

        if (order == null) {
            result.put("acyclic", false);
            result.put("order", new ArrayList<Integer>());
        } else {
            List<Integer> vertices = new ArrayList<>(n);

            for (int vertex : order) {
                vertices.add(vertex);
            }

            result.put("acyclic", true);
            result.put("order", vertices);
        }

        return result;
    }


    private static int[] topologicalOrder(List<List<Integer>> graph) {
        int n = graph.size();

        // 0 = unvisited, 1 = active, 2 = finished
        int[] state = new int[n];
        int[] nextIndex = new int[n];
        int[] stack = new int[n];
        int[] order = new int[n];

        int orderSize = 0;

        for (int start = 0; start < n; start++) {
            if (state[start] != 0) {
                continue;
            }

            int top = 0;
            stack[top++] = start;
            state[start] = 1;

            while (top > 0) {
                int vertex = stack[top - 1];

                if (nextIndex[vertex] < graph.get(vertex).size()) {
                    int next = graph.get(vertex).get(nextIndex[vertex]++);

                    if (state[next] == 1) {
                        return null;
                    }

                    if (state[next] == 0) {
                        state[next] = 1;
                        stack[top++] = next;
                    }
                } else {
                    state[vertex] = 2;
                    order[orderSize++] = vertex;
                    top--;
                }
            }
        }

        int[] result = new int[n];

        for (int i = 0; i < n; i++) {
            result[i] = order[n - 1 - i];
        }

        return result;
    }

    // -------------------- DAG SHORTEST PATHS --------------------

    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> graph = new ArrayList<>();
        List<List<WeightedEdge>> weightedGraph = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
            weightedGraph.add(new ArrayList<>());
        }

        for (int id = 0; id < edges.length; id++) {
            int u = edges[id][0];
            int v = edges[id][1];
            int weight = edges[id][2];

            graph.get(u).add(v);
            weightedGraph.get(u).add(
                    new WeightedEdge(u, v, weight, id));
        }

        int[] order = topologicalOrder(graph);

        Map<String, Object> result = new LinkedHashMap<>();

        if (order == null) {
            result.put("acyclic", false);
            result.put("distance", new ArrayList<Long>());
            result.put("parent_edge", new ArrayList<Integer>());

            return result;
        }

        Long[] distance = new Long[n];
        int[] parentEdge = new int[n];

        Arrays.fill(parentEdge, -1);
        distance[source] = 0L;

        for (int vertex : order) {
            if (distance[vertex] == null) {
                continue;
            }

            for (WeightedEdge edge : weightedGraph.get(vertex)) {
                long candidate = distance[vertex] + edge.weight;

                if (distance[edge.to] == null
                        || candidate < distance[edge.to]) {
                    distance[edge.to] = candidate;
                    parentEdge[edge.to] = edge.id;
                }
            }
        }

        List<Long> distances = new ArrayList<>(n);
        List<Integer> parents = new ArrayList<>(n);

        Collections.addAll(distances, distance);

        for (int edgeId : parentEdge) {
            parents.add(edgeId);
        }

        result.put("acyclic", true);
        result.put("distance", distances);
        result.put("parent_edge", parents);

        return result;
    }

    // -------------------- ROUTE RECONSTRUCTION --------------------

    static List<Integer> reconstructRoute(
            int source,
            int target,
            int[] parentEdge,
            int[][] edges,
            Long[] distance) {

        List<Integer> route = new ArrayList<>();

        if (distance[target] == null) {
            return route;
        }

        int current = target;
        route.add(current);

        int steps = 0;

        while (current != source) {
            int edgeId = parentEdge[current];

            if (edgeId < 0 || edgeId >= edges.length) {
                return new ArrayList<>();
            }

            if (edges[edgeId][1] != current) {
                return new ArrayList<>();
            }

            current = edges[edgeId][0];
            route.add(current);

            steps++;

            if (steps > parentEdge.length) {
                return new ArrayList<>();
            }
        }

        Collections.reverse(route);

        return route;
    }
}

