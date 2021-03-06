import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

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
    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     *
     * @param dbPath Path to the XML file to be parsed.
     */

    private HashMap<Long, Node> IDtoNode = new HashMap();
    /* edges maps a Node to a hashset(neightbor) of its edges */
    private HashMap<Long, HashSet<Edge>> edges = new HashMap<>();
    private HashMap<Long, HashSet<Long>> neighbors = new HashMap<>();
    ArrayList<Node> sortbyXY = new ArrayList<>();
    Node medianx=new Node(38,122,50);

    /* allEdges contains all edges in graph (might not implement*/
    private TreeSet<Edge> allEdges = new TreeSet<>();

    public HashMap<Long, Node> getIDtoNode() {
        return IDtoNode;
    }

    public HashMap<Long, HashSet<Edge>> getEdges() {
        return edges;
    }

    public HashMap<Long, HashSet<Long>> getNeighbors() {
        return neighbors;
    }

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
        for (Node n : IDtoNode.values()) {
            sortbyXY.add(n);
        }
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
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> removeID = new ArrayList<>();
        for (long nodeid : neighbors.keySet()) {
            if (neighbors.get(nodeid).isEmpty()) {
                removeID.add(nodeid);
            }
        }
        for (long nodeid : removeID) {
            neighbors.remove(nodeid);
            IDtoNode.remove(nodeid);
            edges.remove(nodeid);
        }
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {
        return IDtoNode.get(v).getLon();
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {
        return IDtoNode.get(v).getLat();
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        return IDtoNode.keySet();
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     *
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        if (IDtoNode.containsKey(v)) {
            return new HashSet<>(neighbors.get(v));
        }
        return new HashSet<>();
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

    public double distance(double lon1, double lon2, double lat1, double lat2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2 - lat1);
        double dlambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double distance(double lon1, double lat1, long nodeid) {
        return distance(lon1, lon(nodeid), lat1, lat(nodeid));
    }

    /**
     * Returns the ID of the vertex closest to the given longitude and latitude.
     *
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    public long closest(double lon, double lat) {
         //medianx=kdtree(medianx, sortbyXY, 0);//build kd tree
         //double inputx=projectToX(lon,lat);
        // double inputy=projectToY(lon,lat);
        //double dx=inputx-medianx.x;
       // double dy=inputy-medianx.y;
       // double bestdistance=Math.sqrt(dx*dx+dy*dy);
        // return findclosesthelper(medianx.left,medianx,inputx,inputy,bestdistance).id;
        double closest=10000000;
        long closestid=1000000;
        for (long id : IDtoNode.keySet()) {
            if (distance(lon, lat, id) < closest) {
                closest = distance(lon, lat, id);
                closestid = id;
            }
        }
        return closestid;

    }

    public Node findclosesthelper(Node next, Node bestsofar, double inputx, double inputy,double bestdistance){
        double dx=inputx-next.x;
        double dy=inputy-next.y;
        double r=Math.sqrt(dx*dx+dy*dy);
        int axis=next.depth%2;
        if(r>bestdistance){
            if(axis==0){
                if(next.equals(bestsofar.right)) {
                    if (Math.abs(dx) > bestdistance && bestsofar.left != null) {
                        bestsofar = findclosesthelper(bestsofar.left, bestsofar, inputx, inputy, bestdistance);
                    }
                }
                if(next.equals(bestsofar.left)) {
                    if (Math.abs(dx) > bestdistance && bestsofar.right != null) {
                        bestsofar = findclosesthelper(bestsofar.right, bestsofar, inputx, inputy, bestdistance);
                    }
                }
            }else{
                //if ()
            }
        }









        return bestsofar;

    }

    public Node kdtree(Node median, List<Node> sortby, int depth) {
        if (sortby.size() == 1) {
           median=sortby.get(0);
           median.depth=depth;
           return median;
        } else if (depth % 2 == 0) {
            Collections.sort(sortby, new Comparator<Node>() {
                @Override
                public int compare(Node node1, Node node2) {
                    return Double.compare(node1.x, node2.x);
                }
            });

        } else {
            Collections.sort(sortby, new Comparator<Node>() {
                @Override
                public int compare(Node node1, Node node2) {
                    return Double.compare(node1.y, node2.y);
                }

            });
        }

        median = sortby.get(sortby.size() / 2);
        median.depth=depth;
        if(sortby.size()==2){
            ArrayList<Node> left=new ArrayList<>();
            ArrayList<Node> right=new ArrayList<>();
            left.add(sortby.get(0));
            right.add(sortby.get(1));
            median.left =kdtree(median.left,left, depth + 1);
            median.right = kdtree(median.right,right , depth + 1);
        }else{
            median.left =kdtree(median.left,sortby.subList(0, sortby.size() / 2), depth + 1);
            median.right = kdtree(median.right,sortby.subList((sortby.size() /2)+1,sortby.size())  , depth + 1);
        }

        return median;
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

    public void addNode(double lat, double lon, long id) {
        if (!IDtoNode.containsKey(id)) {
            IDtoNode.put(id, new Node(lat, lon, id));
            neighbors.put(id, new HashSet<>());
            edges.put(id, new HashSet<>());
        }

    }

    public void addEdge(long source, long dest) {
        Edge srcTodest = new Edge(source, dest, distance(source, dest));//source to dest edge
        Edge destTosrc = new Edge(dest, source, distance(dest, source));//dest to source edge
        neighbors.get(source).add(dest);
        neighbors.get(dest).add(source);
        edges.get(source).add(srcTodest);
        edges.get(dest).add(destTosrc);
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

    public class Node {
        private double lat;
        private double lon;
        private long id;
        private String Name;
        private double x;
        private double y;
        private Node left;
        private Node right;
        private int depth;

        public Node(double lat, double lon, long id) {
            this.lat = lat;
            this.lon = lon;
            this.id = id;
            this.x = projectToX(lon, lat);
            this.y = projectToY(lon, lat);
        }

        public Node(double lat, double lon, long id, String name) {
            this.lat = lat;
            this.lon = lon;
            this.id = id;
            this.Name = name;
        }

        public void setName(String name) {
            Name = name;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public long getId() {
            return id;
        }


        public String getName() {
            return Name;
        }

    }

    public class Edge implements Comparable<Edge> {

        private long src;
        private long dest;
        private double weight;

        /* Creates an Edge (SRC, DEST) with edge weight WEIGHT. */
        Edge(long src, long dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }

        /* Returns the edge's source node. */
        public long getSource() {
            return src;
        }

        /* Returns the edge's destination node. */
        public long getDest() {
            return dest;
        }

        /* Returns the weight of the edge. */
        public double getWeight() {
            return weight;
        }

        public int compareTo(Edge other) {
            double cmp = weight - other.weight;
            return (int) cmp;
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


    }

}
