import com.sun.source.tree.Tree;

import java.util.*;

/**
 * Uses BFS to return a graph of possible shortest paths, code used from GraphTraversal
 * Gets the shortest path between two vertices from bfs
 * Finds average path lengths
 * Finds out which vertices are infitely separate from a specific node
 * @author Rebecca Liu and Dylan Lawler, Spring 2021
 */

public class GraphLibrary{

    /**
     *
     * @param g graph to be traverses
     * @param source vertex to find shortest path from
     * @param <V> vertex of any type
     * @param <E> edge of any type
     * @return a graph of shortest paths from source
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source){
        Graph<V,E> pathTree = new AdjacencyMapGraph<>(); //initialize backTrack
        Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
        Queue<V> queue = new LinkedList<V>(); //queue to implement BFS

        queue.add(source); //enqueue start vertex
        visited.add(source); //add start to visited Set
        while (!queue.isEmpty()) { //loop until no more vertices
            V u = queue.remove(); //dequeue

                pathTree.insertVertex(u);
            for (V v : g.outNeighbors(u)) { //loop over out neighbors
                if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                    visited.add(v); //add neighbor to visited Set
                    queue.add(v); //enqueue neighbor
                    pathTree.insertVertex(v); // inserts the vertex to the path tree
                    pathTree.insertDirected(v, u, g.getLabel(v, u)); //save that the neighbor vertex was discovered from prior vertex
                }
                }

        }
        return pathTree;
    }

    /**
     *
     * @param tree tree to find path from
     * @param v find path from v to center of the tree
     * @param <V> vertex of any type
     * @param <E> edge of any type
     * @return shortest path between desired vertex and source
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v){

        // if the tree is empty or doesnt have the given vertex, return an empty list
        if (tree.numVertices() == 0 || !tree.hasVertex(v)) {
            return new ArrayList<V>();
        }
        // if the tree has no out neighbors, return an empty list
        if (tree.outDegree(v) == 0) {
            return new ArrayList<V>();
        }
        //start from given vertex and work backward to source vertex
        ArrayList<V> path = new ArrayList<V>(); //this will hold the path from start to end vertex
        V current = v; //start at given vertex
        //loop from given vertex back to source vertex
        while (tree.outDegree(current) != 0 ) {
            path.add(0,current); //add this vertex to front of list path
            for (V neighbor: tree.outNeighbors(current)){
            current = neighbor;} //get vertex that discovered this vertex
        }
        return path;

    }

    /**
     *
     * @param graph original graph with all vertices
     * @param subgraph path graph after bfs, only vertices connected to the source
     * @param <V> vertex of any type
     * @param <E> edge of any type
     * @return a set of the vertices that are infinitely far from the source
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        // initializes a new set of missing vertices
        Set<V> missingVertices = new HashSet<V>();

        // if the subgraph doesn't have any vertices the main graph does, add it to missing vertices set
            for (V v: graph.vertices()){
                if(!subgraph.hasVertex(v)){
                    missingVertices.add(v);
                }
            }
        return missingVertices;
    }

    /**
     *
     * @param tree path tree after bfs
     * @param root vertex finding the average distance from
     * @param <V> vertex of any type
     * @param <E> edge of any type
     * @return a double of the average distances from each vertex to the root
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
        // calls helper function to add up all the distances
    double averageseperation = averageSeparationHelper(tree, root, 0);

    // averages out the distances and returns
        return averageseperation/(tree.numVertices()-1);

    }

    /**
     *
     * @param tree path tree after bfs
     * @param root vertex finding the average distance from
     * @param <V> vertex of any type
     * @param <E> edge of any type
     * @param averageseperation distance measuring double
     * @return the total sum of the distances to be averaged
     */
    public static <V,E> double averageSeparationHelper(Graph<V,E> tree, V root, double averageseperation){
        double avgsep = averageseperation;
        // starts at root
        V current = root;

                // if the vertex has no more children pointing to it, return the sum of the distances
                if (tree.inDegree(current) == 0){
                    return avgsep;
                }
                // if the vertex has a child, recursively find the distance
                else{
                    for(V neighbour: tree.inNeighbors(current)){
                        avgsep = avgsep + averageSeparationHelper(tree, neighbour, averageseperation+1);
                    }
                }
        return avgsep;
    }

    /**
     * hard coding test
     * @param args
     */
    public static void main(String[] args) {
        Graph<String, String> testGraph = new AdjacencyMapGraph<>();
        testGraph.insertVertex("Kevin Bacon");
        testGraph.insertVertex("Alice");
        testGraph.insertVertex("Bob");
        testGraph.insertVertex("Charlie");
        testGraph.insertVertex("Dartmouth");
        testGraph.insertVertex("Nobody");
        testGraph.insertVertex("Nobody's Friend");
        testGraph.insertUndirected("Kevin Bacon", "Bob", "A Movie");
        testGraph.insertUndirected("Kevin Bacon", "Alice", "A Movie");
        testGraph.insertUndirected("Kevin Bacon", "Alice", "E Movie");
        testGraph.insertUndirected("Alice", "Bob", "A Movie");
        testGraph.insertUndirected("Alice", "Charlie", "D Movie");
        testGraph.insertUndirected("Bob", "Charlie", "C Movie");
        testGraph.insertUndirected("Dartmouth", "Charlie", "B Movie");
        testGraph.insertUndirected("Nobody", "Nobody's Friend", "F Movie");
        Graph<String, String> testTree = bfs(testGraph, "Kevin Bacon");
        System.out.println("Hard Coded Graph:");
        System.out.println(testGraph);
        System.out.println("Path between Dartmouth and Kevin Bacon");
        System.out.println(getPath(testTree, "Dartmouth"));
        System.out.println("Infinite far away vertices:");
        System.out.println(missingVertices(testGraph, testTree));
        System.out.println("Average separation from Kevin Bacon:");
        System.out.println(averageSeparation(testTree, "Kevin Bacon"));
    }

}
