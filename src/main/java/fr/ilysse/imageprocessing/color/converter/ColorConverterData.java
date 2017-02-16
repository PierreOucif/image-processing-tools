package fr.ilysse.imageprocessing.color.converter;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by p_poucif on 03/11/2016.
 */
public class ColorConverterData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorConverterData.class);
    private Map<LAB,Color> RGBMappedToLAB;
    private List<LAB> listOfLAB;
    private Map<Color,LAB> LABPMapToRGB;
    private Color[][] colorTwoDim;
    private Map<Integer,Color> rgbCluster;
    private int xMax;
    private int yMax;
    private LAB[][] LABTwoDim;

    public BufferedImage getImagePostKMeans(Map<LAB,Integer> map){
        final BufferedImage finalImage = new BufferedImage(xMax,yMax,BufferedImage.TYPE_INT_RGB);

        for(int x=0;x<xMax;x++){
            for(int y=0;y<yMax;y++){
                Color colorXY = rgbCluster.get(map.get(LABPMapToRGB.get(colorTwoDim[x][y])));
                finalImage.setRGB(x,y,colorXY.getRGB());
            }
        }


        return finalImage;
    }
    private void initializeColorDatas(BufferedImage image){

        LOGGER.info("Start initialization of the color datas ...");
        final Stopwatch timer = Stopwatch.createUnstarted();

        Map<LAB,Color> vRGBMappedToLAB = new HashMap<>();
        List<LAB> vListOfLAB = new ArrayList<>();
        LABPMapToRGB = new HashMap<>();
        xMax = image.getWidth();
        yMax = image.getHeight();
        LOGGER.info("x_max = {} / y_max = {} / nb_pixel = {}",xMax,yMax,xMax*yMax);
        colorTwoDim = new Color[xMax][yMax];
        LABTwoDim = new LAB[xMax][yMax];

        timer.start();
        for(int w=0;w<xMax;w++){
            for(int h=0;h<yMax;h++){
                Color colorWH = new Color(image.getRGB(w,h));
                colorTwoDim[w][h]=colorWH;
                LAB LABWH = new LAB(colorWH);
                LABTwoDim[w][h] = LABWH;
                vRGBMappedToLAB.put(LABWH,colorWH);
                LABPMapToRGB.put(colorWH,LABWH);
                vListOfLAB.add(LABWH);
            }
        }
        RGBMappedToLAB=vRGBMappedToLAB;
        listOfLAB=vListOfLAB;
        timer.stop();
        LOGGER.info("End of the Initialization in {}",timer);
    }

    public ColorConverterData(BufferedImage image){
        initializeColorDatas(image);
        this.rgbCluster = new HashMap<>();
        rgbCluster.put(0,new Color(255,0,0));
        rgbCluster.put(1,new Color(0,255,0));
        rgbCluster.put(2,new Color(0,0,255));
    }

    public Map<LAB,Color> getRGBMappedToLAB(){
        return RGBMappedToLAB;
    }

    public List<LAB> getListOfLAB(){
        return listOfLAB;
    }

    public Map<Color,LAB> getLABPMapToRGB(){return LABPMapToRGB;}

    public LAB[][] getLABTwoDim(){ return LABTwoDim;}
}
