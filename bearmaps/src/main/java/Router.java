import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     *
     * @param g       <code>GraphDB</code> data source.
     * @param stlon   The longitude of the starting coordinate.
     * @param stlat   The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {
        // Implement A* Search, a single-pair shortest path
        // Initialize PriorityQueue, implement own comparator
        Astar shortPath = new Astar(g);
        return shortPath.shortestPath(g, stlon, stlat, destlon, destlat);
    }


    static class Astar {

        GraphDB g;
        PriorityQueue<GraphDB.Vertex> fringe;
        HashMap<Long, Long> path;
        HashSet<Long> visited;
        HashMap<Long, Double> bestNodeDistances;
        HashMap<Long, Double> pqValues;

        Astar(GraphDB g) {
            this.g = g;
            fringe = new PriorityQueue<GraphDB.Vertex>(1, new VertComparator());
            path = new HashMap<>();
            bestNodeDistances = new HashMap<>();
            visited = new HashSet<>();
            pqValues = new HashMap<>();
        }

        public List<Long> shortestPath(GraphDB graph, double stlon, double stlat,
                                       double destlon, double destlat) {
            //Find vertices first
            GraphDB.Vertex startVert = g.getVert(g.closest(stlon, stlat));
            GraphDB.Vertex endVert = g.getVert(g.closest(destlon, destlat));
            fringe.add(startVert);
            path.put(startVert.vertID, startVert.vertID);
            bestNodeDistances.put(startVert.vertID, 0.0);
            pqValues.put(startVert.vertID, euclidian(startVert, endVert));
            while (!fringe.isEmpty()) {
                GraphDB.Vertex currVert = fringe.poll();
                if (visited.contains(currVert.vertID)) {
                    continue;
                }
                if (currVert.equals(endVert)) {
                    break;
                }
                visited.add(currVert.vertID);
                for (Long vertID : g.adjacent(currVert.vertID)) {
                    if (visited.contains(vertID)) {
                        continue;
                    }
                    Double currG = bestNodeDistances.get(currVert.vertID)
                            + g.distance(currVert.vertID, g.getVert(vertID).vertID);
                    //We should be computing hueuristic here
                    if (!bestNodeDistances.containsKey(vertID)) {
                        pqValues.put(vertID, (currG + euclidian(g.getVert(vertID),
                                g.getVert(endVert.vertID))));
                        bestNodeDistances.put(vertID, currG);
                        path.put(vertID, currVert.vertID);
                        fringe.add(g.getVert(vertID));
                        continue;
                    }
                    if (currG < bestNodeDistances.get(vertID)) {
                        pqValues.put(vertID, currG
                                + euclidian(g.getVert(vertID), g.getVert(endVert.vertID)));
                        bestNodeDistances.put(vertID, currG);
                        path.put(vertID, currVert.vertID);
                        fringe.add(g.getVert(vertID));
                    }
                }
            }
            ArrayList<Long> returnPath = new ArrayList<>();
            long current = endVert.vertID;
            int counter = 0;
            while (!(current == startVert.vertID)) {
                returnPath.add(current);
                current = path.get(current);
                //returnPath.add(endVert.vertID);
            }
            returnPath.add(current);
            Collections.reverse(returnPath);
            return returnPath;
        }

        class VertComparator implements Comparator<GraphDB.Vertex> {
            public int compare(GraphDB.Vertex vert1, GraphDB.Vertex vert2) {
                //Compare priority values of vertices
                if (!pqValues.containsKey(vert1.vertID)) {
                    return -1;
                } else if (!pqValues.containsKey(vert2.vertID)) {
                    return 1;
                }
                return pqValues.get(vert1.vertID).compareTo(pqValues.get(vert2.vertID));
            }
        }

        private double euclidian(GraphDB.Vertex currVert, GraphDB.Vertex endVert) {
            double xval = g.projectToX(currVert.lon, currVert.lat)
                    - g.projectToX(endVert.lon, endVert.lat);
            double yval = g.projectToY(endVert.lon, endVert.lat)
                    - g.projectToY(endVert.lon, endVert.lat);
            return Math.sqrt((xval * xval) + (yval * yval));
        }


    }

    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     *
     * @param g     <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        // TODO
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction represented.
         */
        int direction;
        /**
         * The name of this way.
         */
        String way;
        /**
         * The distance along this way.
         */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         *
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
