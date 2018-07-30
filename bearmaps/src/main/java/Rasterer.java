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

    public static double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
    ROOT_LRLAT = 37.82280243352756,  ROOT_LRLON = -122.2119140625;
    public  ArrayList<Double> depth;
    public Rasterer(){
        depth=new ArrayList<>();
        depth.add(0.000002682209014892578);//d7
        depth.add(0.000005364418029785156);//d6
        depth.add(0.000010728836059570312);
        depth.add(0.000021457672119140625);//d4
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
        double ullat= params.ullat;//upper left upprtdown
        double ullon=params.ullon;//upper left leftright
        double lrlat=params.lrlat;
        double lrlon=params.lrlon;
        double w=params.w;
        double h=params.h;
        double CalDpp= lonDPP(lrlon,ullon,w);
        double dp=compare(CalDpp);
        System.out.println(dp);
        if(dp==-1) {
            return RasterResultParams.queryFailed();
        }
        int intdepth=depth.indexOf(dp);
        double[] upperleft=findLeftcorner(ullat,ullon,dp);
        double[] lowerright=findRightcorner(lrlat,lrlon,dp);
        String [][] grid=findGrid(upperleft,lowerright,intdepth);


        return new RasterResultParams(grid,upperleft[0],upperleft[1],lowerright[0],lowerright[1],intdepth,true);
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

    //changed to use depth4;
    private double compare(double lonDPP ){
        System.out.println(lonDPP);
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

    public double[] findLeftcorner(double tile_ullat,double tile_ullon,double dp){
        double[] xy=new double[4];
        double finaly=tile_ullat;
        double finalx=tile_ullon;
        double y=this.ROOT_ULLAT;
        double x=this.ROOT_ULLON;
        int i=0,j=0;
        while(x<finalx){
            x+=dp*256;
            //System.out.println(x);
            System.out.println(i);
            i++;
        }
        while(finaly<y){
            y-=dp*256;
            System.out.println(y);
            j++;
        }
        xy[0]=x-dp*256;
        xy[1]=y+dp*256;
        xy[2]=i-1;
        xy[3]=j-1;

            return xy;
    }

    public double[] findRightcorner(double tile_ullat,double tile_ullon,double dp){
        double[] xy=new double[4];
        double finaly=tile_ullat;
        double finalx=tile_ullon;
        double y=this.ROOT_ULLAT;
        double x=this.ROOT_ULLON;
        int i=0,j=0;
        while(x<finalx){
            x+=dp*256;
            i++;
        }
        while(finaly<y){
            y-=dp*256;
            j++;
        }
        xy[0]=x;
        xy[1]=y;
        xy[2]=i;
        xy[3]=j;
        return xy;
    }
    public String[][] findGrid(double[] ulc,double[] lrc,int intdepth){
        //double truedp=depth.get(intdepth);
        //int pxperrow=(int) Math.floor((ROOT_LRLON-ROOT_ULLON)/truedp);
        //int pxpercol=(int) Math.floor((ROOT_ULLAT-ROOT_LRLAT)/truedp);
         String dp="d"+intdepth;
        String x="_x";
        String y="_y";
        //i and j are flipped
        int row=(int)lrc[3]-(int)ulc[3];
        int col=(int)lrc[2]-(int)ulc[2];
        //int upperleft=(int)ulc[2]+(int)(pxperrow*(int) ulc[3]);
        String[][] grid =new String[row][col];
        int startx=(int)ulc[2];
        int starty=(int)ulc[3];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j <col ; j++) {
                grid[i][j]=(dp+x+(startx+j)+y+(starty+i));
            }
        }
        return grid;
    }


}
