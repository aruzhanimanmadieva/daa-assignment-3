import java.util.Arrays;
import java.util.List;

public class RouteTest {
    public static void main(String[] args) {

        // Test 1: Route to a reachable vertex
        int[][] edges = {
                {0, 1, 3},
                {1, 2, -7},
                {2, 3, 4}
        };

        int[] parentEdge = {-1, 0, 1, 2};
        Long[] distance = {0L, 3L, -4L, 0L};

        List<Integer> route = Algorithms.reconstructRoute(
                0, 3, parentEdge, edges, distance);

        System.out.println("Reachable route: " + route);
        check(route.equals(Arrays.asList(0, 1, 2, 3)),
                "Reachable route");

        // Test 2: Route to the source
        route = Algorithms.reconstructRoute(
                0, 0, parentEdge, edges, distance);

        System.out.println("Source route: " + route);
        check(route.equals(List.of(0)), "Source route");

        // Test 3: Route to an unreachable vertex
        Long[] distanceWithUnreachable = {0L, 3L, -4L, 0L, null};
        int[] parentWithUnreachable = {-1, 0, 1, 2, -1};

        route = Algorithms.reconstructRoute(
                0, 4, parentWithUnreachable, edges, distanceWithUnreachable);

        System.out.println("Unreachable route: " + route);
        check(route.isEmpty(), "Unreachable route");

        // Test 4: Parallel edges
        int[][] parallelEdges = {
                {0, 1, 5},
                {0, 1, 2}
        };

        int[] parallelParent = {-1, 1};
        Long[] parallelDistance = {0L, 2L};

        route = Algorithms.reconstructRoute(
                0, 1, parallelParent, parallelEdges, parallelDistance);

        System.out.println("Parallel-edge route: " + route);
        check(route.equals(List.of(0, 1)), "Parallel-edge route");

        System.out.println("All route tests passed.");
    }

    private static void check(boolean condition, String testName) {
        if (!condition) {
            throw new AssertionError(testName + " failed");
        }
    }


}
