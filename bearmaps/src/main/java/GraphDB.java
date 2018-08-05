import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.List;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Objects;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 */
public class GraphDB {

    /* Implement constructors for previously designed Graph class. */

    /* neighbors maps vertices iD to a map of neighboring vertices */
    private HashMap<Long, Vertex> neighbors = new HashMap<>();

    /* edges maps vertices id to a map of its edges */
    private HashMap<Long, Edge> edges = new HashMap<>();

    /* allEdges contains all edges in graph (might not implement*/
    private HashSet<Edge> allEdges = new HashSet<>();

    private KDT kdTree = new KDT(null);

    private int edgecounter;


    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     *
     * @param dbPath Path to the XML file to be parsed.
     */

    public GraphDB(String dbPath) {
        File inputFile = new File(dbPath);
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        ArrayList<Vertex> listVert = arrayListVertices();
        Collections.sort(listVert, new Vertex.SortByX());
        //sort by uclidean distance from root lot and lan
        KDT kdtCopy = new KDT(null);
        kdTree.root = kdtHelper(listVert, kdtCopy, 0);
    }

    public KdTreeNode kdtHelper(ArrayList<Vertex> listVert, KDT currKDTree, int depth) {
        int axis = depth % 2; //specify how to sort
        // if axis == 0 sort on x, if axis == 1 sort on y
        if (axis == 0) {
            Collections.sort(listVert, new Vertex.SortByX());
        } else if (axis == 1) {
            Collections.sort(listVert, new Vertex.SortByY());
        }
        if (listVert.size() == 1) {
            currKDTree.root = new KdTreeNode(null);
            currKDTree.root = new KdTreeNode(listVert.get(0));
            return currKDTree.root;
        } else if (listVert.size() == 2) {
            currKDTree.root = new KdTreeNode(listVert.get(1));
            currKDTree.root.left = new KDT(listVert.get(0));
            return currKDTree.root;
        } else if (listVert.size() == 3) {
            currKDTree.root = new KdTreeNode(listVert.get(1));
            currKDTree.root.left = new KDT(listVert.get(0));
            currKDTree.root.right = new KDT(listVert.get(2));
            //currKDTree.root.left.root = new KdTreeNode(listVert.get(0));
            //currKDTree.root.right.root = new KdTreeNode(listVert.get(2));
            return currKDTree.root;
        }
        currKDTree.root = new KdTreeNode(listVert.get((listVert.size() / 2)));
        ArrayList<Vertex> first = new ArrayList<>(listVert.subList(0, listVert.size() / 2));
        ArrayList<Vertex> second =
                new ArrayList<>(listVert.subList((listVert.size() / 2) + 1, listVert.size()));
        currKDTree.root.left = new KDT(null);
        currKDTree.root.right = new KDT(null);
        currKDTree.root.left.root = kdtHelper(first, currKDTree.root.left, depth + 1);
        currKDTree.root.right.root = kdtHelper(second, currKDTree.root.right, depth + 1);
        return currKDTree.root;
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Add vertex (node) to graph if it doesn't already exist in graph.  Adopted from
     * lab26 methods
     */
    public void addVert(long vertID, double lat, double lon) {
        if (neighbors.get(vertID) == null) {
            neighbors.put(vertID, new Vertex(lon, lat, vertID));
        }
    }

    /**
     * Return vertex given vertID
     */
    public Vertex getVert(long vertID) {
        return neighbors.get(vertID);
    }

    /**
     * removeVert removes from neighbors HashMap and allVertices HashMap
     */
    public void removeVert(long vertID) {
        if (neighbors.get(vertID) == null) {
            System.out.println("Vertex does not exist in graph");
        } else {
            neighbors.remove(vertID);
            //allEdges.remove(edges.get(vertID));
            //edges.remove(vertID);
        }
    }

    public void addVertHelper(Vertex vert) {
        addVert(vert.vertID, vert.lat, vert.lon);
    }

    public void addEdgeHelper(Edge e) {
        addEdge(e.src, e.dest, e.weight);
    }

    public void addEdge(long vertID1, long vertID2, double weight) {
        /*addVertHelper(neighbors.get(vertID1)); // add vertices if they don't already exist
        addVertHelper(neighbors.get(vertID2)); *///
        Edge e1 = new Edge(vertID1, vertID2, weight);
        Edge e2 = new Edge(vertID2, vertID1, weight);
        edges.put(vertID1, e1);
        edges.put(vertID2, e2);
        allEdges.add(e1);
        neighbors.get(vertID1).vertNeighbors.add(vertID2);
        neighbors.get(vertID2).vertNeighbors.add(vertID1);
    }

    public TreeSet<Long> getAllVert() {
        return new TreeSet<Long>(neighbors.keySet());
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> removeIDs = new ArrayList<>();
        for (Long vertices : vertices()) {
            if (neighbors.get(vertices).vertNeighbors.isEmpty()) {
                removeIDs.add(vertices);
            }
        }
        for (Long vertIDRemove : removeIDs) {
            removeVert(vertIDRemove);
        }
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {
        return neighbors.get(v).lon;
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {
        return neighbors.get(v).lat;
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        return neighbors.keySet();
    }

    ArrayList<Vertex> arrayListVertices() {
        ArrayList<Vertex> returnList = new ArrayList<>();
        Iterator<Long> iterator = vertices().iterator();
        while (iterator.hasNext()) {
            returnList.add(getVert(iterator.next()));
        }
        return returnList;
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     *
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        if (neighbors.containsKey(v)) {
            return neighbors.get(v).vertNeighbors;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     *
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Returns the ID of the vertex closest to the given longitude and latitude.
     *
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    public long closest(double lon, double lat) {
        int depth = 0;
        Vertex vertDesired = new Vertex(lon, lat, -00);
        addVert(-00, lat, lon);
        KdTreeNode closestNode = closestHelper(depth, vertDesired, kdTree, kdTree.root);
        removeVert(-00);
        return closestNode.vert.vertID;
    }


    public KdTreeNode closestHelper(int depth, Vertex searchVert, KDT tree, KdTreeNode best) {
        int axis = depth % 2;
        double queryX = projectToX(searchVert.lon, searchVert.lat);
        double queryY = projectToY(searchVert.lon, searchVert.lat);
        KdTreeNode currentBest = best;
        if (tree.root.left == null && tree.root.right == null) {
            return tree.root;
        }
        if (axis == 0) {
            if (!(tree.root.left == null)) {
                KdTreeNode currentNode = tree.root;
                currentBest = closestHelper(depth + 1, searchVert, tree.root.left, currentNode);
                double euclidCurrNode = euclidean(currentNode.x, queryX, currentNode.y, queryY);
                double euclidBestNode = euclidean(currentBest.x, queryX, currentBest.y, queryY);
                double gcCurrNode = distance(currentNode.vert.vertID, searchVert.vertID);
                double gcBestNode = distance(currentBest.vert.vertID, searchVert.vertID);
                double newMin = euclidBestNode;
                if (euclidCurrNode < euclidBestNode) {
                    newMin = euclidCurrNode;
                }
                if (gcCurrNode < gcBestNode) {
                    currentBest = currentNode;
                }
                if (euclidean(currentBest.x, queryX, 0, 0) < newMin) {
                    KdTreeNode currentRoot = currentBest;
                    if (!(tree.root.right == null)) {
                        KdTreeNode currentBest2 = closestHelper
                                (depth + 1, searchVert, tree.root.right, currentRoot);
                        double gcCurrRoot = distance(searchVert.vertID, currentRoot.vert.vertID);
                        double gcBestNode2 = distance(currentBest2.vert.vertID, searchVert.vertID);
                        if (gcCurrRoot < gcBestNode2) {
                            return currentRoot;
                        }
                        return currentBest2;
                    }
                }
                return currentBest;
            }
        }
        if (axis == 1) {
            if (!(tree.root.left == null)) {
                KdTreeNode currentNode = tree.root;
                currentBest = closestHelper(depth + 1, searchVert, tree.root.left, currentNode);
                double euclidCurrNode = euclidean(currentNode.x, queryX, currentNode.y, queryY);
                double euclidBestNode = euclidean(currentBest.x, queryX, currentBest.y, queryY);
                double gcCurrNode = distance(currentNode.vert.vertID, searchVert.vertID);
                double gcBestNode = distance(currentBest.vert.vertID, searchVert.vertID);
                double newMin = euclidBestNode;
                if (euclidCurrNode < euclidBestNode) {
                    newMin = euclidCurrNode;
                }
                if (gcCurrNode < gcBestNode) {
                    currentBest = currentNode;
                }
                if (euclidean(0, 0, currentBest.y, queryY) < newMin) { // check if in other tree
                    KdTreeNode currentRoot = currentBest;
                    if (!(tree.root.right == null)) {
                        KdTreeNode currentBest2 = closestHelper
                                (depth + 1, searchVert, tree.root.right, currentRoot);
                        double gcCurrRoot = distance(currentRoot.vert.vertID, searchVert.vertID);
                        double gcBestNode2 = distance(currentBest2.vert.vertID, searchVert.vertID);
                        if (gcCurrRoot < gcBestNode2) {
                            return currentRoot;
                        }
                        return currentBest2;
                    }
                }
            }
        }
        return currentBest;
    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {
        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     *
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Radius of the Earth in miles.
     */
    private static final int R = 3963;
    /**
     * Latitude centered on Berkeley.
     */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /**
     * Longitude centered on Berkeley.
     */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     *
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;

    /**
     * Add Sub-Classes to make storage and other processes easier.  Consider classes
     * encountered before.  An Edge and Node(Vertex Class) seem most helpful.  Use
     * Vertex sub-class, consider values needed
     * lon, lat, id.
     * Include Edge subclass to make the formation of edges easier
     */

    static Double euclidean(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    static class Vertex { //note vertex is equivalent to node
        double lon;
        double lat;
        long vertID;
        String vertName;
        HashSet<Long> vertNeighbors;

        Vertex(double lon, double lat, long vertID) {
            this.lon = lon;
            this.lat = lat;
            this.vertID = vertID;
            this.vertNeighbors = new HashSet<Long>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Vertex vertex = (Vertex) o;
            return Double.compare(vertex.lon, lon) == 0
                    && Double.compare(vertex.lat, lat) == 0
                    && vertID == vertex.vertID
                    && Objects.equals(vertName, vertex.vertName)
                    && Objects.equals(vertNeighbors, vertex.vertNeighbors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lon, lat, vertID, vertName, vertNeighbors);
        }

        static class SortByEuclid implements Comparator<Vertex> {
            public int compare(Vertex a, Vertex b) {
                double vertAx = projectToX(a.lon, a.lat);
                double vertAy = projectToY(a.lon, a.lat);
                double vertBx = projectToX(b.lon, b.lat);
                double vertBy = projectToY(b.lon, b.lat);
                double rootX = projectToX(ROOT_LON, ROOT_LAT);
                double rootY = projectToY(ROOT_LON, ROOT_LAT);
                return euclidean(vertAx, rootX, vertAy, rootY).compareTo(
                        euclidean(vertBx, rootX, vertBy, rootY));
            }
        }

        static class SortByX implements Comparator<Vertex> {
            public int compare(Vertex a, Vertex b) {
                Double vertAx = projectToX(a.lon, a.lat);
                Double vertBx = projectToX(b.lon, b.lat);
                return vertAx.compareTo(vertBx);
            }
        }

        static class SortByY implements Comparator<Vertex> {
            public int compare(Vertex a, Vertex b) {
                Double vertAy = projectToY(a.lon, a.lat);
                Double vertBy = projectToY(b.lon, b.lat);
                return vertAy.compareTo(vertBy);
            }
        }

    }

    static class KdTreeNode {

        GraphDB.Vertex vert;
        Double x;
        Double y;
        KDT left;
        KDT right;

        KdTreeNode(Vertex vert) {
            this.vert = vert;
            if (!(vert == null)) {
                this.x = projectToX(vert.lon, vert.lat);
                this.y = projectToY(vert.lon, vert.lat);
            } else {
                this.x = 0.0;
                this.y = 0.0;
            }
            left = null;
            right = null;
        }

        public double euclideanKD(double lon, double lat) {
            double x2 = projectToX(lon, lat);
            double y2 = projectToY(lon, lat);
            return Math.sqrt(Math.pow(this.x - x2, 2) + Math.pow(this.y - y2, 2));
        }
    }

    static class KDT {
        KdTreeNode root;

        KDT(Vertex root) {
            this.root = new KdTreeNode(root);
        }
    }

    static class Edge implements Comparable<Edge> {
        /* Note that all roads are considered two way.  We can therefore
         * thinks of "roads" as undirected edges. Only need two vertices
         * Designate vertOne and vertTwo
         * Implement Comparable to allow for easier sorting later when
         * finding shortest path*/

        long src;
        long dest;
        double weight;

        Edge(long src, long dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }

        public int compareTo(Edge other) {
            double cmp = weight - other.weight;
            return (int) Math.round(cmp);
        }

        /* Returns true if two Edges have the same source, destination, and
           weight. */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Edge e = (Edge) o;
            return (src == e.src && dest == e.dest && weight == e.weight)
                    || (src == e.dest && dest == e.src && weight == e.weight);
        }

        @Override
        public int hashCode() {

            return Objects.hash(src, dest, weight);
        }
        /* Returns the hashcode for this instance. */

    }
}