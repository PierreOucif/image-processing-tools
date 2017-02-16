package fr.ilysse.imageprocessing;

import fr.ilysse.imageprocessing.image.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final URL CURRENT_PATH = App.class.getClassLoader().getResource(".");
    @SuppressWarnings({"all"})
    public static void main(String[] args) throws Exception, IOException, URISyntaxException {

        Files.newDirectoryStream(Paths.get(CURRENT_PATH.toURI()),"*.{jpg}").forEach((Path dir)->{
            BufferedImage imageBuffered = null;

            try{
                imageBuffered = ImageIO.read(dir.toFile());
//                ImageUtils.displayImage(imageBuffered);
//                ImageUtils.displayImage(ImageUtils.getComputedImageWithBlackMargins(imageBuffered,843,843,178,1100,1200));
//                ImageUtils.displayImageWithMargin(imageBuffered);
//                ImageUtils.displayBWImage(ImageUtils.getGreyImage(imageBuffered));
//                ImageUtils.getBoundedImage(imageBuffered);
//                ImageUtils.displayImage(imageBuffered);
//                ImageUtils.displayBoundedImage(imageBuffered);


//                ColorConverterData colorDatas = new ColorConverterData(imageBuffered);
//
//                ClusterUtils clusterUtils = new ClusterUtils();
//
//                List<LAB> computedLABs = clusterUtils.executeKMeans(3,3,colorDatas.getListOfLAB(),false);
//
//                BufferedImage imageClustered= ImageUtils.getClusteredImage(imageBuffered, colorDatas.getLABPMapToRGB());
//                ImageUtils.displayImage(imageClustered);
//
//                BufferedImage imageWithoutBackground = ImageUtils.getImageWithoutBackground(imageBuffered,colorDatas.getLABPMapToRGB());
//                ImageUtils.displayImage(imageWithoutBackground);
//                ImageUtils.displayImage(ImageUtils.getComputedImageWithBlackMargins(imageBuffered,810,520,178,1100,1200));
//                ImageUtils.displayImage(ImageUtils.getComputedImage(imageBuffered,810,520,178,1100,1200));
//                ImageUtils.getBackgroundColor(imageBuffered);


//                CannyEdgeDetector detector = new CannyEdgeDetector();
//                detector.setLowThreshold(0.1f);
//                detector.setHighThreshold(2f);
//
//                detector.setSourceImage(imageBuffered);
//                detector.process();
//                ImageUtils.displayImage(detector.getEdgesImage());

//                ImageUtils.displayImage(ImageUtils.getComputedImageWithEdgeDetection(imageBuffered,810,520,178,1100,1200));
                BufferedImage finalBufferedImage =ImageUtils.getComputedImageWithLABClusters(imageBuffered,653,653,178,1100,1200);
                String temp = System.getProperty("java.io.tmpdir");
                File outputFile = new File(temp+"resultat_POC5.jpg");
                LOGGER.info("Writting resulting image to {}",outputFile);
                ImageIO.write(finalBufferedImage,"jpg",outputFile);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }












        });
    }

}
