package fr.ilysse.imageprocessing.image;

import com.google.common.base.Stopwatch;
import fr.ilysse.imageprocessing.clustering.ClusterUtils;
import fr.ilysse.imageprocessing.color.converter.ColorConverterData;
import fr.ilysse.imageprocessing.color.converter.LAB;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by p_poucif on 10/11/2016.
 */
public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);
    private static final int X_SCALE = 2;
    private static final int Y_SCALE = 3;
    private static final double DELTA_WHITE = 0.1;
    private static final float R_GREY_SCALE = 0.2126F;
    private static final float G_GREY_SCALE = 0.7152F;
    private static final float B_GREY_SCALE = 0.0722F;
    private static final int DELTA_MARGIN = 5;

    public static void displayLAB(List<LAB> listOfLABs) {
        try {
            Map<String, Object> map = getXYOfLAB(listOfLABs, getLABBornes(listOfLABs));
            BufferedImage bufferedImage = getImageFromXYLAB(map);
            displayImage(bufferedImage);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

    }


    public static Long[][] getLABBornes(List<LAB> listOfLABs) {
        LOGGER.info("Looking for the 2D lab bounds of {} lab colors", listOfLABs.size());
        Long[][] results = new Long[2][2];
        LAB lab = null;
        double xmax = 0;
        double xmin = 0;
        double ymin = 0;
        double ymax = 0;
        for (int i = 0; i < listOfLABs.size(); i++) {
            lab = listOfLABs.get(i);
            if (i == 0) {
                xmax = xmin = lab.getA();
                ymax = ymin = lab.getB();
            } else {
                xmax = Double.compare(xmax, lab.getA()) < 0 ? lab.getA() : xmax;
                xmin = Double.compare(xmin, lab.getA()) > 0 ? lab.getA() : xmin;
                ymax = Double.compare(ymax, lab.getB()) < 0 ? lab.getB() : ymax;
                ymin = Double.compare(ymin, lab.getB()) > 0 ? lab.getB() : ymin;
            }
        }
        results[0][0] = Math.round(xmin - 1);
        results[0][1] = Math.round(xmax + 1);
        results[1][0] = Math.round(ymin - 1);
        results[1][1] = Math.round(ymax + 1);
        return results;
    }


    public static Map<String, Object> getXYOfLAB(List<LAB> listOfLAB, Long[][] bornes) throws Exception {
        int xmin = X_SCALE * Math.toIntExact(bornes[0][0]);
        int xmax = X_SCALE * Math.toIntExact(bornes[0][1]);
        int ymin = Y_SCALE * Math.toIntExact(bornes[1][0]);
        int ymax = Y_SCALE * Math.toIntExact(bornes[1][1]);

        int xScale = Math.toIntExact(xmax - xmin);
        int yScale = Math.toIntExact(ymax - ymin);

        LAB[][] labImage = new LAB[xScale][yScale];

        for (LAB lab : listOfLAB) {
            int a = Math.toIntExact(Math.round(X_SCALE * lab.getA())) + Math.abs(xmin);
            int b = Math.toIntExact(Math.round(Y_SCALE * lab.getB())) + Math.abs(ymin);
            if (a >= 0 && a <= xmax + Math.abs(xmin)) {
                if (b >= 0 && b <= ymax + Math.abs(ymin)) {
                    labImage[a][b] = lab;
                } else {
                    throw new Exception("b value out of the bounds : " + b);
                }
            } else {
                throw new Exception("a value out of the bounds : " + a);
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("xScale", xScale);
        resultMap.put("yScale", yScale);
        resultMap.put("labImage", labImage);
        return resultMap;

    }

    public static BufferedImage getImageFromXYLAB(Map<String, Object> objectsMap) {
        BufferedImage image = new BufferedImage((int) objectsMap.get("xScale"), (int) objectsMap.get("yScale"), BufferedImage.TYPE_INT_RGB);

        LAB[][] lab = (LAB[][]) objectsMap.get("labImage");
        LOGGER.info("Using lab 2D image of size : {}", lab.length * lab[0].length);

        for (int x = 0; x < (int) objectsMap.get("xScale"); x++) {
            for (int y = 0; y < (int) objectsMap.get("yScale"); y++) {
                if (lab[x][y] != null) {
                    image.setRGB(x, y, getRGBForCluster(lab[x][y].getClusterIndex()));
                }
            }
        }

        return image;

    }

    public static int getRGBForCluster(Integer nbCluster) {
        if (nbCluster == 1) {
            return Color.RED.getRGB();
        } else if (nbCluster == 2) {
            return Color.GREEN.getRGB();
        } else if (nbCluster == 3) {
            return Color.BLUE.getRGB();
        } else if (nbCluster == 0) {
            return Color.WHITE.getRGB();
        } else {
            return Color.BLACK.getRGB();
        }
    }


    public static BufferedImage getClusteredImage(BufferedImage initialeImage, Map<Color, LAB> colorsMapToLABs) {
        final int X_MAX = initialeImage.getWidth();
        final int Y_MAX = initialeImage.getHeight();
        BufferedImage image = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < X_MAX; x++) {
            for (int y = 0; y < Y_MAX; y++) {
                LAB lab = colorsMapToLABs.get(new Color(initialeImage.getRGB(x, y)));
                image.setRGB(x, y, getRGBForCluster(lab.getClusterIndex()));
            }
        }
        return image;
    }

    public static BufferedImage getImageWithoutBackground(BufferedImage initialeImage, Map<Color, LAB> colorsMapToLABs, int backgroundClusterIndex) {
        final int X_MAX = initialeImage.getWidth();
        final int Y_MAX = initialeImage.getHeight();
        for (int x = 0; x < X_MAX; x++) {
            for (int y = 0; y < Y_MAX; y++) {
                LAB lab = colorsMapToLABs.get(new Color(initialeImage.getRGB(x, y)));
                if (lab.getClusterIndex() == backgroundClusterIndex) {
                    initialeImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return initialeImage;
    }

    public static void displayImage(BufferedImage bufferedImage) {
        JFrame frameInit = new JFrame();
        JLabel labelInit = new JLabel(new ImageIcon(bufferedImage));
        frameInit.setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
        frameInit.add(labelInit);
        frameInit.setVisible(true);
    }

    public static boolean[][] getBWFromRGB(BufferedImage bufferedImage) {
        boolean[][] computedImage = new boolean[bufferedImage.getWidth()][bufferedImage.getHeight()];
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (Double.compare(DELTA_WHITE, compareRGB(Color.WHITE, new Color(bufferedImage.getRGB(x, y)))) <= 0) {
                    computedImage[x][y] = true;
                } else {
                    computedImage[x][y] = false;
                }
            }
        }
        return computedImage;
    }

    public static boolean[][] getWBFromRGB(BufferedImage bufferedImage) {
        boolean[][] computedImage = new boolean[bufferedImage.getWidth()][bufferedImage.getHeight()];
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (bufferedImage.getRGB(x, y) == -1) {
                    computedImage[x][y] = true;
                } else {
                    computedImage[x][y] = false;
                }
            }
        }
        return computedImage;
    }


    protected static double compareRGB(Color color1, Color color2) {
        return Math.sqrt(Math.pow(color1.getRed() - color2.getRed(), 2)
                + Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + Math.pow(color1.getBlue() - color2.getBlue(), 2));
    }


    public static BufferedImage getBWImage(BufferedImage bufferedImage) {
        BufferedImage finalBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        boolean[][] boolean2D = getBWFromRGB(bufferedImage);
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (boolean2D[x][y]) {
                    finalBufferedImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    finalBufferedImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return finalBufferedImage;
    }

    public static void displayBWImage(BufferedImage bufferedImage) {
        displayImage(getBWImage(bufferedImage));
    }

    public static BufferedImage getGreyScaleImage(BufferedImage bufferedImage) {
        int grey = 0;
        Color color = null;
        BufferedImage finalBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                color = new Color(bufferedImage.getRGB(x, y));
                grey = Math.round(R_GREY_SCALE * color.getRed() + G_GREY_SCALE * color.getGreen() + B_GREY_SCALE * color.getBlue());
                color = new Color(grey, grey, grey);
                finalBufferedImage.setRGB(x, y, color.getRGB());
            }
        }
        return finalBufferedImage;
    }

    public static void displayGreyScaleImage(BufferedImage bufferedImage) {
        displayImage(getGreyScaleImage(bufferedImage));
    }

    public static BufferedImage getGreyImage(BufferedImage bufferedImage) {
        int grey = 0;
        Color color = null;
        BufferedImage finalBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                color = new Color(bufferedImage.getRGB(x, y));
                grey = Math.round((color.getRed() + color.getGreen() + color.getBlue()) / 3);
                color = new Color(grey, grey, grey);
                finalBufferedImage.setRGB(x, y, color.getRGB());
            }
        }
        return finalBufferedImage;

    }

    public static void displayGreyImage(BufferedImage bufferedImage) {
        displayImage(getGreyImage(bufferedImage));
    }


//    public static BufferedImage getBoundedImage(BufferedImage bufferedImage){
//        boolean[][] booleanImage = getBWFromRGB(bufferedImage);
//        LOGGER.info("{} px * {} px",bufferedImage.getWidth(),bufferedImage.getHeight());
//        BufferedImage cuttedImage = cutImageFromBounds(bufferedImage,getXYBounds(booleanImage));
//        return cuttedImage;
//    }
//
//    public static void displayBoundedImage(BufferedImage bufferedImage){
//        displayImage(getBoundedImage(bufferedImage));
//    }

    public static BufferedImage cutImageFromBounds(BufferedImage bufferedImage, int xMin, int xMax, int yMin, int yMax) {

        BufferedImage cuttedImage = new BufferedImage(xMax - xMin, yMax - yMin, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < xMax - xMin; x++) {
            for (int y = 0; y < yMax - yMin; y++) {
                cuttedImage.setRGB(x, y, bufferedImage.getRGB(x + xMin, y + yMin));
            }
        }

        return cuttedImage;
    }

    public static BufferedImage cutImageFromCenter(BufferedImage bufferedImage, int xTemplate, int yTemplate) {
        if (bufferedImage.getWidth() > xTemplate || bufferedImage.getHeight() > yTemplate) {
            LOGGER.error("The image to cut is bigger than the corresponding template");
            return null;
        } else {
            LOGGER.info("{} px * {} px image to {} px * {} px image", bufferedImage.getWidth(), bufferedImage.getHeight(), xTemplate, yTemplate);
            BufferedImage cuttedImage = new BufferedImage(xTemplate, yTemplate, BufferedImage.TYPE_INT_RGB);
            int x1 = (xTemplate - bufferedImage.getWidth()) / 2;
            int x2 = xTemplate - x1 - 1;
            int y1 = (yTemplate - bufferedImage.getHeight()) / 2;
            int y2 = yTemplate - y1 - 1;
            for (int x = 0; x < xTemplate; x++) {
                for (int y = 0; y < yTemplate; y++) {
                    if (x > x1 && x < x2 && y > y1 && y < y2) {
                        if (x - x1 >= bufferedImage.getWidth() || y - y1 >= bufferedImage.getHeight()) {
                            LOGGER.error("x or y out of image boundaries while getting the centered image");
                            return null;
                        }
                        cuttedImage.setRGB(x, y, bufferedImage.getRGB(x - x1, y - y1));
                    } else {
                        cuttedImage.setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
            return cuttedImage;
        }

    }


    public static BufferedImage getSquareImageWithMargin(BufferedImage image) {
        BufferedImage resultingImage = null;
        int width = image.getWidth();
        int height = image.getHeight();
        LOGGER.info("Add margin to get square image from initial image {} px * {} px", width, height);
        if (width > height) {
            resultingImage = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
            int y1 = (width - height) / 2;
            int y2 = y1 + height - 1;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    if (y < y1 || y > y2) {
                        resultingImage.setRGB(x, y, Color.WHITE.getRGB());
                    } else {
                        resultingImage.setRGB(x, y, image.getRGB(x, y - y1));
                    }
                }

            }

            return resultingImage;
        } else if (width < height) {
            resultingImage = new BufferedImage(height, height, BufferedImage.TYPE_INT_RGB);
            int x1 = (height - width) / 2;
            int x2 = x1 + width - 1;
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < height; y++) {
                    if (x < x1 || x > x2) {
                        resultingImage.setRGB(x, y, Color.WHITE.getRGB());
                    } else {
                        resultingImage.setRGB(x, y, image.getRGB(x - x1, y));
                    }
                }
            }
            return resultingImage;
        } else {
            return image;
        }
    }

    public static void displaySquareImageWithMargin(BufferedImage bufferedImage) {
        displayImage(getSquareImageWithMargin(bufferedImage));
    }


    public static Map<String, Integer> getXYBounds(boolean[][] bwImage) {
        int xMin = bwImage.length;
        int xMax = 0;
        int yMin = bwImage[0].length;
        int yMax = 0;
        int xCentroid = 0;
        int yCentroid = 0;
        int nb = 0;

        for (int x = 0; x < bwImage.length; x++) {
            for (int y = 0; y < bwImage[0].length; y++) {
                if (bwImage[x][y]) {
                    xCentroid += x;
                    yCentroid += y;
                    nb++;
                    if (y >= yMax) {
                        yMax = y;
                    } else if (y <= yMin) {
                        yMin = y;
                    }

                }
            }
        }

        for (int y = 0; y < bwImage[0].length; y++) {
            for (int x = 0; x < bwImage.length; x++) {
                if (bwImage[x][y]) {
                    if (x >= xMax) {
                        xMax = x;
                    } else if (x <= xMin) {
                        xMin = x;
                    }
                }
            }
        }

        xCentroid = Math.round(xCentroid / nb);
        yCentroid = Math.round(yCentroid / nb);

//        LOGGER.info("xMin = {} & xMax = {}", xMin, xMax);
//        LOGGER.info("yMin = {} & yMax = {}", yMin, yMax);
//        LOGGER.info("xCentroid = {} & yCentroid = {} for a total of {} black pixels analyzed", xCentroid, yCentroid, nb);

        Map<String, Integer> results = new HashMap<>();
        results.put("xMin", xMin);
        results.put("xMax", xMax);
        results.put("xCentroid", xCentroid);
        results.put("yMin", yMin);
        results.put("yMax", yMax);
        results.put("yCentroid", yCentroid);
        results.put("nb", nb);

        return results;
    }

    public static Map<String, Integer> getXYBoundsFromRGBImage(BufferedImage bufferedImage) {
        displayImage(bufferedImage);
        int xMin = bufferedImage.getWidth();
        int xMax = 0;
        int yMin = bufferedImage.getHeight();
        int yMax = 0;
        int xCentroid = 0;
        int yCentroid = 0;
        int nb = 0;

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (bufferedImage.getRGB(x, y) == Color.WHITE.getRGB()) {
                    xCentroid += x;
                    yCentroid += y;
                    nb++;
                    if (y >= yMax) {
                        yMax = y;
                    } else if (y <= yMin) {
                        yMin = y;
                    }

                }
            }
        }

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                if (bufferedImage.getRGB(x, y) == Color.WHITE.getRGB()) {
                    if (x >= xMax) {
                        xMax = x;
                    } else if (x <= xMin) {
                        xMin = x;
                    }
                }
            }
        }

        xCentroid = Math.round(xCentroid / nb);
        yCentroid = Math.round(yCentroid / nb);

        LOGGER.info("xMin = {} & xMax = {}", xMin, xMax);
        LOGGER.info("yMin = {} & yMax = {}", yMin, yMax);
        LOGGER.info("xCentroid = {} & yCentroid = {} for a total of {} black pixels analyzed", xCentroid, yCentroid, nb);

        Map<String, Integer> results = new HashMap<>();
        results.put("xMin", xMin);
        results.put("xMax", xMax);
        results.put("xCentroid", xCentroid);
        results.put("yMin", yMin);
        results.put("yMax", yMax);
        results.put("yCentroid", yCentroid);
        results.put("nb", nb);

        return results;
    }

    public static Map<String, Integer> getXYBoundsWithoutBackground(BufferedImage bufferedImage, Integer colorInt) {
        int xMin = bufferedImage.getWidth();
        int xMax = 0;
        int yMin = bufferedImage.getHeight();
        int yMax = 0;
        int xCentroid = 0;
        int yCentroid = 0;
        int nb = 0;

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (bufferedImage.getRGB(x, y) != colorInt) {
                    xCentroid += x;
                    yCentroid += y;
                    nb++;
                    if (y >= yMax) {
                        yMax = y;
                    } else if (y <= yMin) {
                        yMin = y;
                    }

                }
            }
        }

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                if (bufferedImage.getRGB(x, y) != colorInt) {
                    if (x >= xMax) {
                        xMax = x;
                    } else if (x <= xMin) {
                        xMin = x;
                    }
                }
            }
        }

        xCentroid = Math.round(xCentroid / nb);
        yCentroid = Math.round(yCentroid / nb);

        LOGGER.info("xMin = {} & xMax = {}", xMin, xMax);
        LOGGER.info("yMin = {} & yMax = {}", yMin, yMax);
        LOGGER.info("xCentroid = {} & yCentroid = {} for a total of {} black pixels analyzed", xCentroid, yCentroid, nb);

        Map<String, Integer> results = new HashMap<>();
        results.put("xMin", xMin);
        results.put("xMax", xMax);
        results.put("xCentroid", xCentroid);
        results.put("yMin", yMin);
        results.put("yMax", yMax);
        results.put("yCentroid", yCentroid);
        results.put("nb", nb);

        return results;
    }

    public static Map<String, Integer> getXYBoundsWithLAB(BufferedImage bufferedImage) {
        ColorConverterData datas = new ColorConverterData(bufferedImage);
        ClusterUtils clusterUtils = new ClusterUtils();
        clusterUtils.executeKMeans(2, 3, datas.getListOfLAB(), true);

        LAB[][] labImage = datas.getLABTwoDim();
//        displayImage(getClusteredImage(bufferedImage,datas.getLABPMapToRGB()));
        int backgroundClusterIndex = getBackgroundClusterIndex(labImage);
//        getImageWithoutBackground(bufferedImage, datas.getLABPMapToRGB(), backgroundClusterIndex);

        int xMin = labImage.length;
        int xMax = 0;
        int yMin = labImage[0].length;
        int yMax = 0;
        int xCentroid = 0;
        int yCentroid = 0;
        int nb = 0;

        for (int x = 0; x < labImage.length; x++) {
            for (int y = 0; y < labImage[0].length; y++) {
                if (labImage[x][y].getClusterIndex() != backgroundClusterIndex) {
                    xCentroid += x;
                    yCentroid += y;
                    nb++;
                    if (y >= yMax) {
                        yMax = y;
                    } else if (y <= yMin) {
                        yMin = y;
                    }

                }
            }
        }

        for (int y = 0; y < labImage[0].length; y++) {
            for (int x = 0; x < labImage.length; x++) {
                if (labImage[x][y].getClusterIndex() != backgroundClusterIndex) {
                    if (x >= xMax) {
                        xMax = x;
                    } else if (x <= xMin) {
                        xMin = x;
                    }
                }
            }
        }

        xCentroid = Math.round(xCentroid / nb);
        yCentroid = Math.round(yCentroid / nb);

        LOGGER.info("xMin = {} & xMax = {}", xMin, xMax);
        LOGGER.info("yMin = {} & yMax = {}", yMin, yMax);
        LOGGER.info("xCentroid = {} & yCentroid = {} for a total of {} black pixels analyzed", xCentroid, yCentroid, nb);

        Map<String, Integer> results = new HashMap<>();
        results.put("xMin", xMin);
        results.put("xMax", xMax);
        results.put("xCentroid", xCentroid);
        results.put("yMin", yMin);
        results.put("yMax", yMax);
        results.put("yCentroid", yCentroid);
        results.put("nb", nb);

        return results;
    }


    public static BufferedImage getComputedImage(BufferedImage bufferedImage, int xTemplate, int yTemplate, int yMargin, int outputWidth, int outputHeight) {
        int minDimTemplate = xTemplate <= yTemplate ? xTemplate : yTemplate;
        boolean[][] bwFromRGB = getBWFromRGB(getGreyImage(bufferedImage));
        Map<String, Integer> initialImageDatas = getXYBounds(bwFromRGB);
        int xMin = initialImageDatas.get("xMin");
        int xMax = initialImageDatas.get("xMax");
        int yMin = initialImageDatas.get("yMin");
        int yMax = initialImageDatas.get("yMax");
        int xImage = xMax - xMin;
        int yImage = yMax - yMin;

        if (xImage > yImage) {
            minDimTemplate = xTemplate;
        } else if (yImage > xImage) {
            minDimTemplate = yTemplate;
        }

        BufferedImage cuttedImageFromBounds = cutImageFromBounds(bufferedImage, xMin, xMax, yMin, yMax);
        BufferedImage squareImageWithMargin = getSquareImageWithMargin(cuttedImageFromBounds);
        BufferedImageOp op = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BILINEAR);
        BufferedImage resize = Scalr.resize(squareImageWithMargin, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.BEST_FIT_BOTH,
                minDimTemplate, minDimTemplate, op);
        if (resize != null) {
            BufferedImage finalImage = getFinalImageFromCenterImage(outputWidth, outputHeight, yMargin, resize);
            return finalImage;
        } else {
            return null;
        }
    }

    public static void displayComputedImage(BufferedImage bufferedImage, int xTemplate, int yTemplate, int yMargin, int outputWidth, int outputHeight) {
        displayImage(getComputedImage(bufferedImage, xTemplate, yTemplate, yMargin, outputWidth, outputHeight));
    }


    public static BufferedImage getComputedImageWithBlackMargins(BufferedImage bufferedImage, int xTemplate, int yTemplate, int yMargin, int outputWidth, int outputHeight) {
        int minDimTemplate = xTemplate <= yTemplate ? xTemplate : yTemplate;
        boolean[][] bwFromRGB = getBWFromRGB(getGreyImage(bufferedImage));
        Map<String, Integer> initialImageDatas = getXYBounds(bwFromRGB);
        int xMin = initialImageDatas.get("xMin");
        int xMax = initialImageDatas.get("xMax");
        int yMin = initialImageDatas.get("yMin");
        int yMax = initialImageDatas.get("yMax");
        int xImage = xMax - xMin;
        int yImage = yMax - yMin;

        if (xImage > yImage) {
            minDimTemplate = xTemplate;
        } else if (yImage > xImage) {
            minDimTemplate = yTemplate;
        }

        BufferedImage cuttedImageFromBounds = cutImageFromBounds(bufferedImage, xMin, xMax, yMin, yMax);
        BufferedImage squareImageWithMargin = getSquareImageWithMargin(cuttedImageFromBounds);
        BufferedImageOp op = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BILINEAR);
        BufferedImage resize = Scalr.resize(squareImageWithMargin, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.BEST_FIT_BOTH,
                minDimTemplate, minDimTemplate, op);
        if (resize != null) {
            BufferedImage finalImage = getFinalImageFromCenterImageWithBackMargins(outputWidth, outputHeight, yMargin, resize);
            return finalImage;
        } else {
            return null;
        }
    }


    public static BufferedImage getComputedImageWithLABClusters(BufferedImage bufferedImage, int xTemplate, int yTemplate, int yMargin, int outputWidth, int outputHeight) {
        BufferedImage croppedImage = getCroppedImageWithConvovle(bufferedImage, getSharpenMask());
        Map<String, Integer> initialImageDatas = getXYBoundsWithLAB(croppedImage);
        int xMin = initialImageDatas.get("xMin");
        int xMax = initialImageDatas.get("xMax");
        int yMin = initialImageDatas.get("yMin");
        int yMax = initialImageDatas.get("yMax");
        BufferedImage cuttedImageFromBounds = cutImageFromBounds(bufferedImage, xMin, xMax, yMin, yMax);


        int maxDimTemplate = xTemplate >= yTemplate ? xTemplate : yTemplate;
        int maxDimImage = bufferedImage.getWidth() >= cuttedImageFromBounds.getHeight() ? cuttedImageFromBounds.getWidth() : cuttedImageFromBounds.getHeight();
        int maxDim;
        if (maxDimTemplate == xTemplate && maxDimImage == cuttedImageFromBounds.getWidth()) {
            maxDim = xTemplate;
        } else if (maxDimTemplate == yTemplate && maxDimImage == cuttedImageFromBounds.getHeight()) {
            maxDim = yTemplate;
        } else if (maxDimTemplate == xTemplate && maxDimImage == cuttedImageFromBounds.getHeight()) {
            maxDim = yTemplate;
        } else if (maxDimTemplate == yTemplate && maxDimImage == cuttedImageFromBounds.getWidth()) {
            maxDim = xTemplate;
        } else {
            maxDim = maxDimTemplate;
        }

        BufferedImage resizedImage = resizeImage(getSquareImageWithMargin(cuttedImageFromBounds), maxDim, maxDim);
//        BufferedImage cuttedImageFromBounds = cutImageFromBounds(resizedImage,xMin,xMax,yMin,yMax);
//        BufferedImage cuttedImage = cutImageFromCenter(cuttedImageFromBounds,xTemplate,yTemplate);

        BufferedImage finalImage = getFinalImageFromCenterImageWithBackMargins(outputWidth, outputHeight, yMargin, resizedImage);
        return finalImage;
    }


    public static BufferedImage getComputedImageWithEdgeDetection(BufferedImage bufferedImage, int xTemplate, int yTemplate, int yMargin, int outputWidth, int outputHeight) {
        Integer backgroundColor = getBackgroundColor(bufferedImage);
        Map<String, Integer> initialImageDatas = getXYBoundsWithoutBackground(bufferedImage, backgroundColor);
        int xMin = initialImageDatas.get("xMin");
        int xMax = initialImageDatas.get("xMax");
        int yMin = initialImageDatas.get("yMin");
        int yMax = initialImageDatas.get("yMax");
        BufferedImage cuttedImageFromBounds = cutImageFromBounds(bufferedImage, xMin, xMax, yMin, yMax);
        displayImage(cuttedImageFromBounds);
        int maxDimTemplate = xTemplate >= yTemplate ? xTemplate : yTemplate;
        int maxDimImage = cuttedImageFromBounds.getWidth() >= cuttedImageFromBounds.getHeight() ? cuttedImageFromBounds.getWidth() : cuttedImageFromBounds.getHeight();
        int maxDim;
        if (maxDimTemplate == xTemplate && maxDimImage == cuttedImageFromBounds.getWidth()) {
            maxDim = xTemplate;
        } else if (maxDimTemplate == yTemplate && maxDimImage == cuttedImageFromBounds.getHeight()) {
            maxDim = yTemplate;
        } else if (maxDimTemplate == xTemplate && maxDimImage == cuttedImageFromBounds.getHeight()) {
            maxDim = yTemplate;
        } else if (maxDimTemplate == yTemplate && maxDimImage == cuttedImageFromBounds.getWidth()) {
            maxDim = xTemplate;
        } else {
            maxDim = maxDimTemplate;
        }

//        BufferedImage resizedImage = resizeImage(getSquareImageWithMargin(cuttedImageFromBounds),maxDim,maxDim);
//        displayImage(resizedImage);
        BufferedImage finalImage = null;

//        BufferedImage resizedImage = resizeImage(getSquareImageWithMargin(bufferedImage),maxDim,maxDim);
//        Map<String,Integer> initialImageDatas = getXYBounds(getBWFromRGB(resizedImage));
//        int xMin = initialImageDatas.get("xMin");
//        int xMax = initialImageDatas.get("xMax");
//        int yMin = initialImageDatas.get("yMin");
//        int yMax = initialImageDatas.get("yMax");
//
//        BufferedImage cuttedImageFromBounds = cutImageFromBounds(resizedImage,xMin,xMax,yMin,yMax);
//        BufferedImage cuttedImage = cutImageFromCenter(cuttedImageFromBounds,xTemplate,yTemplate);
//        BufferedImage finalImage = getFinalImageFromCenterImageWithBackMargins(outputWidth,outputHeight,yMargin,cuttedImage);
        return finalImage;
    }


    public static void displayComputedImageWithBlackMargins(BufferedImage bufferedImage, int xTemplate, int yTemplate, int yMargin, int outputWidth, int outputHeight) {
        displayImage(getComputedImageWithBlackMargins(bufferedImage, xTemplate, yTemplate, yMargin, outputWidth, outputHeight));
    }


    public static BufferedImage resizeImage(BufferedImage initialImage, final int finalWidth, final int finalHeight) {
        BufferedImage finalImage = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
        LOGGER.info("Cutted image => {} px * {} px", initialImage.getWidth(), initialImage.getHeight());
        LOGGER.info("Final image  => {} px * {} px", finalWidth, finalHeight);
        final float RX = ((float) initialImage.getWidth()) / ((float) finalWidth);
        final float RY = ((float) initialImage.getHeight()) / ((float) finalHeight);
        final float C = 0.99999F;
        LOGGER.info("Resize ratio rX = {} & rY = {}", RX, RY);

        for (int x = 0; x < finalWidth; x++) {
            float x1 = x * RX;
            float x2 = x1 + RX;
            int i1 = (int) Math.floor(x1);
            int i2 = (int) Math.ceil(x2) - 1;

            for (int y = 0; y < finalHeight; y++) {
                float y1 = y * RY;
                float y2 = y1 + RY;
                int j1 = (int) Math.floor(y1);
                int j2 = (int) Math.ceil(y2) - 1;

                float currentR = 0F;
                float currentG = 0F;
                float currentB = 0F;
                float r0 = 0F;
                for (int i = i1; i < i2; i++) {
                    float dx = 1F;

                    if (x1 > i) {
                        dx = dx - (x1 - i);
                    } else if (x2 < i + 1) {
                        dx = dx - (i + 1 - x2);
                    }

                    for (int j = j1; j < j2; j++) {
                        float dy = 1F;

                        if (y1 > j) {
                            dy = dy - (y1 - j);
                        } else if (y2 < j + 1) {
                            dy = dy - (j + 1 - y2);
                        }

                        float r = dx * dy;
                        r0 += r;
                        Color currentRGB = new Color(initialImage.getRGB(i, j));
                        currentR += r * currentRGB.getRed();
                        currentG += r * currentRGB.getGreen();
                        currentB += r * currentRGB.getBlue();
                    }
                }

                Color newColor = new Color(Math.round(currentR / r0), Math.round(currentG / r0), Math.round(currentB / r0));
                finalImage.setRGB(x, y, newColor.getRGB());
            }
        }
        return finalImage;
    }

    public static void displayResizedImage(BufferedImage initialImage, int reziseWidth, int reziseHeight) {
        displayImage(resizeImage(initialImage, reziseWidth, reziseHeight));
    }


    public static BufferedImage getFinalImageFromCenterImage(int xFinal, int yFinal, int yMargin, BufferedImage centerImage) {
        BufferedImage finalImage = new BufferedImage(xFinal, yFinal, BufferedImage.TYPE_INT_RGB);

        int x1 = (xFinal - centerImage.getWidth()) / 2;
        int x2 = x1 + centerImage.getWidth() - 1;
        int y1 = (yFinal - centerImage.getHeight()) / 2 + yMargin;
        int y2 = y1 + centerImage.getHeight() - 1;

        for (int x = 0; x < xFinal; x++) {
            for (int y = 0; y < yFinal; y++) {
                if (x > x1 && x < x2 && y > y1 && y < y2) {
                    if (x - x1 < centerImage.getWidth() - 1 && y - y1 < centerImage.getHeight() - 1) {
                        finalImage.setRGB(x, y, centerImage.getRGB(x - x1, y - y1));
                    } else {
                        return null;
                    }
                } else {
                    finalImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return finalImage;
    }

    public static BufferedImage getFinalImageFromCenterImageWithBackMargins(int xFinal, int yFinal, int yMargin, BufferedImage centerImage) {
        BufferedImage finalImage = new BufferedImage(xFinal, yFinal, BufferedImage.TYPE_INT_RGB);

        int x1 = (xFinal - centerImage.getWidth()) / 2;
        int x2 = x1 + centerImage.getWidth() - 1;
        int y1 = (yFinal - centerImage.getHeight()) / 2 + yMargin;
        int y2 = y1 + centerImage.getHeight() - 1;

        for (int x = 0; x < xFinal; x++) {
            for (int y = 0; y < yFinal; y++) {
                if (x > x1 && x < x2 && y > y1 && y < y2) {
                    if (x - x1 < centerImage.getWidth() - 1 && y - y1 < centerImage.getHeight() - 1) {
                        finalImage.setRGB(x, y, centerImage.getRGB(x - x1, y - y1));
                    } else {
                        return null;
                    }
                } else if (x == x1 || x == x2 || y == y1 || y == y2) {
                    finalImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    finalImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return finalImage;
    }

    public static Integer getBackgroundColor(BufferedImage image) {
        Map<Integer, Integer> colorMap = new HashMap<>();

        for (int x = 0; x < image.getWidth(); x++) {
            if (x < DELTA_MARGIN || x > image.getWidth() - DELTA_MARGIN) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (y < DELTA_MARGIN || y > image.getHeight() - DELTA_MARGIN) {
                        int color = image.getRGB(x, y);
                        if (!colorMap.containsKey(color)) {
                            colorMap.put(color, 1);
                        } else {
                            Integer t = colorMap.get(color);
                            colorMap.replace(color, t, t + 1);
                        }
                    }
                }
            }
        }

        int max = 0;
        int colorMax = 0;
        for (Map.Entry entry : colorMap.entrySet()) {
            if ((int) entry.getValue() > colorMax) {
                max = (int) entry.getKey();
            }
        }
        LOGGER.info("Background color {} => {} iterations", max, colorMap.get(max));
        return max;
    }

    public static int getBackgroundClusterIndex(LAB[][] labImage) {
        Map<LAB, Integer> labMap = new HashMap<>();

        for (int x = 0; x < labImage.length; x++) {
            if (x < DELTA_MARGIN || x > labImage.length - DELTA_MARGIN) {
                for (int y = 0; y < labImage[0].length; y++) {
                    if (y < DELTA_MARGIN || y < labImage[0].length - DELTA_MARGIN) {
                        if (!labMap.containsKey(labImage[x][y])) {
                            labMap.put(labImage[x][y], 1);
                        } else {
                            Integer t = labMap.get(labImage[x][y]);
                            labMap.replace(labImage[x][y], t, t + 1);
                        }
                    }
                }
            }
        }

        LAB labMAx = null;
        int max = 0;
        for (Map.Entry entry : labMap.entrySet()) {
            if ((int) entry.getValue() > max) {
                labMAx = (LAB) entry.getKey();
            }
        }
        return labMAx.getClusterIndex();
    }


    public static Kernel getEdgeDetectionMask() {
        float f = 2F;
        float[] floats = {0F, -f, 0F, -f, 4f * f, -f, 0F, -f, 0F};
        return new Kernel(3, 3, floats);
    }

    public static Kernel getSharpenMask() {
        float[] floats = {-1f, -1f, -1f, -1f, 9f, -1f, -1f, -1f, -1f};
        return new Kernel(3, 3, floats);
    }

    public static BufferedImage getCroppedImageWithConvovle(BufferedImage image, Kernel kernel) {
        ConvolveOp convolveOp = new ConvolveOp(kernel);
        return convolveOp.filter(image, null);
    }

    public static BufferedImage getCannyEdgeImage(BufferedImage image) {
        CannyEdgeDetector detector = new CannyEdgeDetector();
        detector.setLowThreshold(0.2f);
        detector.setHighThreshold(1f);

        detector.setSourceImage(image);
        detector.process();
        return detector.getEdgesImage();
    }


}
