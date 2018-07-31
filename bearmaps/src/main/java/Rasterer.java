import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /** The max image depth level. */
    public static final int MAX_DEPTH = 7;
    public  ArrayList<Double> depth;
    public static final double latNaught = 37.892195547244356;
    public static final double latFinal = 37.82280243352756;
    public static final double lonNaught = -122.2998046875;
    public static final double lonFinal = -122.2119140625;

    public static final double d1latDiff = latNaught - 37.85749899038596;
    public static final double d1lonDiff = lonNaught - -122.255859375;

    public static final double d2latDiff = latNaught - 37.87484726881516;
    public static final double d2lonDiff = lonNaught - -122.27783203125;

    public static final double d3latDiff = latNaught - 37.88352140802976;
    public static final double d3lonDiff = lonNaught - -122.288818359375;

    public static final double d4latDiff = latNaught - 37.88785847763706;
    public static final double d4lonDiff = lonNaught - -122.2943115234375;

    public static final double d5latDiff = latNaught - 37.89002701244071;
    public static final double d5lonDiff = lonNaught - -122.29705810546875;

    public static final double d6latDiff = latNaught - 37.891111279842534;
    public static final double d6lonDiff = lonNaught - -122.29843139648438;

    public static final double d7latDiff = latNaught - 37.891653413543445;
    public static final double d7lonDiff = lonNaught - -122.29911804199219;

    public Rasterer(){
        depth=new ArrayList<>();
        depth.add(0.000002682209014892578);//d7
        depth.add(0.000005364418029785156);//d6
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
     *     <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     *     possible, while still covering less than or equal to the amount of longitudinal distance
     *     per pixel in the query box for the user viewport size.</li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the above
     *     condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    public RasterResultParams getMapRaster(RasterRequestParams params) {

        double ullat= params.ullat;
        double ullon=params.ullon;
        double lrlat=params.lrlat;
        double lrlon=params.lrlon;
        double w=params.w;
        double h=params.h;
        double CalDpp= lonDPP(lrlon,ullon,w);
        double comp = compare(CalDpp);

        //CalDpp is the calculated Dpp and gives us the proper depth to use when figuring out which images to present
        if(compare(CalDpp)==-1) {
            return RasterResultParams.queryFailed();
        }
        /* Section to Present right images
        * Given the depth we can determine how wide each box is
        * Access library of sizes built in constructor*/

        //Write method to determine which difference to use
        System.out.println();
        double latDiff = determineLatDiff(comp);
        double lonDiff = determineLonDiff(comp);
        System.out.println("This is latDiff " + latDiff);
        System.out.println("This is lonDiff " + lonDiff);
        if (latDiff == 0.0 && lonDiff == 0.0) {

        }
        //save latitude edge;
        int counterTop = 0;
        int counterBottom = 0;
        int counterLeft = 0;
        int counterRight = 0;
        int SaveCounterTop = 0 ;
        int SaveCounterBottom = 0;
        int SaveCounterRight = 0;
        int SaveCounterLeft = 0;
        double rullat = 0;
        double rlrlat = 0;
        double rullon = 0;
        double rlrlon = 0;

        //Given differences find image that corresponds to edge of query box
        for (double i = latNaught; i > latFinal; i = i - latDiff) {
            if (i > ullat && i - latDiff < ullat) {
                SaveCounterTop = counterTop;
                rullat = i;
            }
            if (i > lrlat && i - latDiff < lrlat) {
                SaveCounterBottom = counterBottom+1;
                rlrlat = i - latDiff;
            }
            counterTop++;
            counterBottom++;
        }
        for (double i = lonNaught; i <= lonFinal; i = i - lonDiff) {
            if (i < ullon && i - lonDiff > ullon) {
                SaveCounterLeft = counterLeft;
                rullon = i;
            }
            if (i < lrlon && i - lonDiff > lrlon) {
                SaveCounterRight = counterRight+1;
                rlrlon = i-lonDiff;
            }
            counterLeft++;
            counterRight++;
        }


        //Now create grid
        int d = determineDepthForGrid(comp);
        String[][] stringGrid = new String[SaveCounterBottom-SaveCounterTop][SaveCounterRight-SaveCounterLeft];
        int storeHorzIndex = 0;
        int storeVertIndex = 0;
        System.out.println("This is save counter left " + SaveCounterLeft);
        System.out.println("This is save counter right" + SaveCounterRight);
        System.out.println("This is save counter top " + SaveCounterTop);
        System.out.println("This is save counter bottom" + SaveCounterBottom);
        for (int j = SaveCounterLeft; j < SaveCounterRight; j++) {
            for (int i = SaveCounterTop; i < SaveCounterBottom; i++ ) {
                stringGrid[storeHorzIndex][storeVertIndex] = "d" + d + "_x" + j + "_y" + i + ".png";
                storeHorzIndex++;
                /*if (storeHorzIndex == SaveCounterRight-SaveCounterLeft) {
                    System.out.println("First For Loop broken");
                    break;
                }*/
            }
            storeHorzIndex = 0;
            storeVertIndex++;
        }
        /*stringGrid[0][1] = "d4_x11_y3.png";
        stringGrid[0][2] = "d4_x12_y3.png";*/

        System.out.println("rullon "+ rullon);
        System.out.println("rlrlat "+ rlrlat);
        System.out.println("rlrlon "+ rlrlon);
        System.out.println("rullat "+ rullat);
        System.out.println();
        RasterResultParams.Builder returnVal = new RasterResultParams.Builder();
        returnVal.setRenderGrid(stringGrid);
        returnVal.setDepth(d);
        returnVal.setQuerySuccess(true);
        returnVal.setRasterUlLon(rullon);
        returnVal.setRasterLrLat(rlrlat);
        returnVal.setRasterLrLon(rlrlon);
        returnVal.setRasterUlLat(rullat);
        /*returnVal.setRasterLrLat(37.85749899038596);
        returnVal.setRasterLrLon(-122.2174072265625);
        returnVal.setRasterUlLat(37.879184338422455);*/
        return returnVal.create();
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    private double compare(double lonDPP ){

        if (lonDPP <= depth.get(0)) {
            return depth.get(0);
        } else if (lonDPP >depth.get(0)&& lonDPP <=depth.get(1) ) {
            return depth.get(0);
        } else if (lonDPP > depth.get(1) && lonDPP <= depth.get(2)) {
            return depth.get(1);
        } else if (lonDPP > depth.get(2) && lonDPP <=depth.get(3)) {
            return depth.get(2);
        } else if (lonDPP > depth.get(3) && lonDPP <=depth.get(4)) {
            return depth.get(3);
        } else if (lonDPP > depth.get(4) && lonDPP <=depth.get(5)) {
            return depth.get(4);
        } else if (lonDPP > depth.get(5) && lonDPP <=depth.get(6)){
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
