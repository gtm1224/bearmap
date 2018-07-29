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
        System.out.println(
                "Since you haven't implemented getMapRaster, nothing is displayed in the browser.");

        /* TODO: Make sure you can explain every part of the task before you begin.
         * Hint: Define additional classes to make it easier to pass around multiple values, and
         * define additional methods to make it easier to test and reason about code. */

        double ullat= params.ullat;
        double ullon=params.ullon;
        double lrlat=params.lrlat;
        double lrlon=params.lrlon;
        double w=params.w;
        double h=params.h;
        double CalDpp= lonDPP(lrlon,ullon,w);
        if(compare(CalDpp)==-1) {
            return RasterResultParams.queryFailed();
        }
        return
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
            return depth.get(1);
        } else if (lonDPP > depth.get(1) && lonDPP <= depth.get(2)) {
            return depth.get(2);
        } else if (lonDPP > depth.get(2) && lonDPP <=depth.get(3)) {
            return depth.get(3);
        } else if (lonDPP > depth.get(3) && lonDPP <=depth.get(4)) {
            return depth.get(4);
        } else if (lonDPP > depth.get(4) && lonDPP <=depth.get(5)) {
            return depth.get(5);
        } else if (lonDPP > depth.get(5) && lonDPP <=depth.get(6)){
            return depth.get(6);
        } else {
        return -1;
    }
    }


}
