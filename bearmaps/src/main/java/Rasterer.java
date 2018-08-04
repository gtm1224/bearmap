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
    private ArrayList<Double> depth;
    private static double latNaught = 37.892195547244356;
    private static double latFinal = 37.82280243352756;
    private static double lonNaught = -122.2998046875;
    private static double lonFinal = -122.2119140625;

    private static double d1latDiff = latNaught - 37.85749899038596;
    private static double d1lonDiff = lonNaught - -122.255859375;

    private static double d2latDiff = latNaught - 37.87484726881516;
    private static double d2lonDiff = lonNaught - -122.27783203125;

    private static double d3latDiff = latNaught - 37.88352140802976;
    private static double d3lonDiff = lonNaught - -122.288818359375;

    private static double d4latDiff = latNaught - 37.88785847763706;
    private static double d4lonDiff = lonNaught - -122.2943115234375;

    private static double d5latDiff = latNaught - 37.89002701244071;
    private static double d5lonDiff = lonNaught - -122.29705810546875;

    private static double d6latDiff = latNaught - 37.891111279842534;
    private static double d6lonDiff = lonNaught - -122.29843139648438;

    private static double d7latDiff = latNaught - 37.891653413543445;
    private static double d7lonDiff = lonNaught - -122.29911804199219;

    public Rasterer() {
        depth = new ArrayList<>();
        depth.add(0.000002682209014892578); //d7
        depth.add(0.000005364418029785156); //d6
        depth.add(0.000010728836059570312);
        depth.add(0.000021457672119140625);
        depth.add(0.00004291534423828125);
        depth.add(0.0000858306884765625);
        depth.add(0.000171661376953125);
        depth.add(0.00034332275390625);
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
        double ullat = params.ullat;
        double ullon = params.ullon;
        double lrlat = params.lrlat;
        double lrlon = params.lrlon;
        double w = params.w;
        double h = params.h;
        double calDpp = lonDPP(lrlon, ullon, w);
        double comp = compare(calDpp);
        if (compare(calDpp) == -1) {
            return RasterResultParams.queryFailed();
        }
        double latDiff = determineLatDiff(comp);
        double lonDiff = determineLonDiff(comp);
        System.out.println("This is latDiff " + latDiff);
        System.out.println("This is lonDiff " + lonDiff);
        int counterTop = 0;
        int counterBottom = 0;
        int counterLeft = 0;
        int counterRight = 0;
        int saveCounterTop = 0;
        int saveCounterBottom = 0;
        int saveCounterRight = 0;
        int saveCounterLeft = 0;
        double rullat = 0;
        double rlrlat = 0;
        double rullon = 0;
        double rlrlon = 0;
        for (double i = latNaught; i > latFinal; i = i - latDiff) {
            if (i > ullat && i - latDiff < ullat) {
                saveCounterTop = counterTop;
                rullat = i;
            }
            if (i > lrlat && i - latDiff < lrlat) {
                saveCounterBottom = counterBottom + 1;
                rlrlat = i - latDiff;
            }
            counterTop++;
            counterBottom++;
        }
        for (double i = lonNaught; i <= lonFinal; i = i - lonDiff) {
            if (i < ullon && i - lonDiff > ullon) {
                saveCounterLeft = counterLeft;
                rullon = i;
            }
            if (i < lrlon && i - lonDiff > lrlon) {
                saveCounterRight = counterRight + 1;
                rlrlon = i - lonDiff;
            }
            counterLeft++;
            counterRight++;
        }
        int d = determineDepthForGrid(comp);
        String[][] stringGrid =
                new String[saveCounterBottom - saveCounterTop][saveCounterRight - saveCounterLeft];
        int storeHorzIndex = 0;
        int storeVertIndex = 0;
        for (int j = saveCounterLeft; j < saveCounterRight; j++) {
            for (int i = saveCounterTop; i < saveCounterBottom; i++) {
                stringGrid[storeHorzIndex][storeVertIndex] = "d" + d + "_x" + j + "_y" + i + ".png";
                storeHorzIndex++;
            }
            storeHorzIndex = 0;
            storeVertIndex++;
        }
        RasterResultParams.Builder returnVal = new RasterResultParams.Builder();
        returnVal.setRenderGrid(stringGrid);
        returnVal.setDepth(d);
        returnVal.setQuerySuccess(true);
        returnVal.setRasterUlLon(rullon);
        returnVal.setRasterLrLat(rlrlat);
        returnVal.setRasterLrLon(rlrlon);
        returnVal.setRasterUlLat(rullat);
        return returnVal.create();
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

    private double compare(double lonDPP) {

        if (lonDPP <= depth.get(0)) {
            return depth.get(0);
        } else if (lonDPP > depth.get(0) && lonDPP <= depth.get(1)) {
            return depth.get(0);
        } else if (lonDPP > depth.get(1) && lonDPP <= depth.get(2)) {
            return depth.get(1);
        } else if (lonDPP > depth.get(2) && lonDPP <= depth.get(3)) {
            return depth.get(2);
        } else if (lonDPP > depth.get(3) && lonDPP <= depth.get(4)) {
            return depth.get(3);
        } else if (lonDPP > depth.get(4) && lonDPP <= depth.get(5)) {
            return depth.get(4);
        } else if (lonDPP > depth.get(5) && lonDPP <= depth.get(6)) {
            return depth.get(5);
        } else {
            return -1;
        }
    }

    private double determineLatDiff(double inPutdepth) {
        if (inPutdepth == depth.get(0)) {
            return d7latDiff;
        } else if (inPutdepth == depth.get(1)) {
            return d6latDiff;
        } else if (inPutdepth == depth.get(2)) {
            return d5latDiff;
        } else if (inPutdepth == depth.get(3)) {
            return d4latDiff;
        } else if (inPutdepth == depth.get(4)) {
            return d3latDiff;
        } else if (inPutdepth == depth.get(5)) {
            return d2latDiff;
        } else if (inPutdepth == depth.get(6)) {
            return d1latDiff;
        } else {
            return 0;
        }
    }

    private double determineLonDiff(double inPutdepth) {
        if (inPutdepth == depth.get(0)) {
            return d7lonDiff;
        } else if (inPutdepth == depth.get(1)) {
            return d6lonDiff;
        } else if (inPutdepth == depth.get(2)) {
            return d5lonDiff;
        } else if (inPutdepth == depth.get(3)) {
            return d4lonDiff;
        } else if (inPutdepth == depth.get(4)) {
            return d3lonDiff;
        } else if (inPutdepth == depth.get(5)) {
            return d2lonDiff;
        } else if (inPutdepth == depth.get(6)) {
            return d1lonDiff;
        } else {
            return 0;
        }
    }

    private int determineDepthForGrid(double inputDepth) {
        if (inputDepth == depth.get(0)) {
            return 7;
        } else if (inputDepth == depth.get(1)) {
            return 6;
        } else if (inputDepth == depth.get(2)) {
            return 5;
        } else if (inputDepth == depth.get(3)) {
            return 4;
        } else if (inputDepth == depth.get(4)) {
            return 5;
        } else if (inputDepth == depth.get(5)) {
            return 6;
        } else {
            return 7;
        }
    }

}
