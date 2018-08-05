import java.util.ArrayList;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /**
     * The max image depth level.
     */
    public static final int MAX_DEPTH = 7;

    private static double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
    private ArrayList<Double> depth;
    private static double converter = (ROOT_LRLON - ROOT_ULLON) / (ROOT_ULLAT - ROOT_LRLAT);


    public Rasterer() {
        depth = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            double d = (ROOT_LRLON - ROOT_ULLON) / (256 * Math.pow(2, i));
            depth.add(i, d);
        }
    }


    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     * possible, while still covering less than or equal to the amount of longitudinal distance
     * per pixel in the query box for the user viewport size.</li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the above
     * condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    public RasterResultParams getMapRaster(RasterRequestParams params) {

        /*
         * Hint: Define additional classes to make it easier to pass around multiple values, and
         * define additional methods to make it easier to test and reason about code. */
        double ullat = params.ullat; //upper left upprtdown
        double ullon = params.ullon; //upper left leftright
        double lrlat = params.lrlat;
        double lrlon = params.lrlon;
        double w = params.w;
        double h = params.h;
        double calDpp = lonDPP(lrlon, ullon, w);
        double dp = compare(calDpp);
        boolean success = true;
        if (dp == -1) {
            return RasterResultParams.queryFailed();
        }
        int intdepth = depth.indexOf(dp);
        double[] upperleft = findLeftcorner(ullat, ullon, dp);
        double[] lowerright = findRightcorner(lrlat, lrlon, dp);
        String[][] grid = findGrid(upperleft, lowerright, intdepth);

        RasterResultParams.Builder returnVal = new RasterResultParams.Builder();
        returnVal.setRenderGrid(grid);
        returnVal.setDepth(intdepth);
        returnVal.setQuerySuccess(success);
        returnVal.setRasterUlLon(upperleft[0]);
        returnVal.setRasterLrLat(lowerright[1]);
        returnVal.setRasterLrLon(lowerright[0]);
        returnVal.setRasterUlLat(upperleft[1]);
        return returnVal.create();


        //return new RasterResultParams(grid,upperleft[0],upperleft[1],
        // lowerright[0],lowerright[1],intdepth,true);
    }

    /**
     * Calculates the lonDPP of an image or query box
     *
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    //changed to use depth4;
    private double compare(double lonDPP) {
        if (lonDPP > depth.get(0)) {
            return depth.get(0);
        } else if (lonDPP > depth.get(1) && lonDPP <= depth.get(0)) {
            return depth.get(1);
        } else if (lonDPP > depth.get(2) && lonDPP <= depth.get(1)) {
            return depth.get(2);
        } else if (lonDPP > depth.get(3) && lonDPP <= depth.get(2)) {
            return depth.get(3);
        } else if (lonDPP > depth.get(4) && lonDPP <= depth.get(3)) {
            return depth.get(4);
        } else if (lonDPP > depth.get(5) && lonDPP <= depth.get(4)) {
            return depth.get(5);
        } else if (lonDPP > depth.get(6) && lonDPP <= depth.get(5)) {
            return depth.get(6);
        } else if (lonDPP > depth.get(7) && lonDPP <= depth.get(6)) {
            return depth.get(7);
        } else {
            return depth.get(7);
        }
    }

    public double[] findLeftcorner(double tileullat, double tileullon, double dp) {
        double[] xy = new double[4];
        double y = ROOT_ULLAT;
        double x = ROOT_ULLON;
        int i;
        int j;
        i = (int) Math.ceil((tileullon - x) / (256 * dp));
        j = (int) Math.ceil((y - tileullat) / (256 * dp / converter));
        xy[0] = x + dp * 256 * (i - 1);
        xy[1] = y - (dp * 256 / converter) * (j - 1);
        xy[2] = i - 1;
        xy[3] = j - 1;
        return xy;
    }

    public double[] findRightcorner(double tileullat, double tileullon, double dp) {
        double[] xy = new double[4];
        double y = this.ROOT_ULLAT;
        double x = this.ROOT_ULLON;
        int i;
        int j;
        i = (int) Math.ceil((tileullon - x) / (256 * dp));
        j = (int) Math.ceil((y - tileullat) / (256 * dp / converter));
        xy[0] = x + (256 * dp) * i;
        xy[1] = y - (256 * dp / converter) * j;
        xy[2] = i;
        xy[3] = j;
        return xy;
    }

    public String[][] findGrid(double[] ulc, double[] lrc, int intdepth) {
        String dp = "d" + intdepth;
        String x = "_x";
        String y = "_y";
        int row = (int) lrc[3] - (int) ulc[3];
        int col = (int) lrc[2] - (int) ulc[2];
        String[][] grid = new String[row][col];
        int startx = (int) ulc[2];
        int starty = (int) ulc[3];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                grid[i][j] = (dp + x + (startx + j) + y + (starty + i) + ".png");
            }
        }
        return grid;
    }


}