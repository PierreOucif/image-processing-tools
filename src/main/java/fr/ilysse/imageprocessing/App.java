package fr.ilysse.imageprocessing;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import fr.ilysse.imageprocessing.data.CSVReader;
import fr.ilysse.imageprocessing.data.DataToProcess;
import fr.ilysse.imageprocessing.data.Template;
import fr.ilysse.imageprocessing.image.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @SuppressWarnings({"all"})
    public static void main(String[] args) {


        Stopwatch timer = Stopwatch.createStarted();
        int imageProcessedCount = 0;
        if (args.length < 1) {
            LOGGER.error("Missing application properties file");
            System.exit(-1);
        }

        Properties properties = null;
        try {
            properties = new Properties();
            properties.load(Files.newInputStream(Paths.get(args[0])));
        } catch (IOException e) {
            LOGGER.error("Unable to read application properties file", e);
            System.exit(-1);
        }

        String csvFile = properties.getProperty("csvFile");
        String csvFileSeparator = properties.getProperty("csvFileSeparator");
        String sourceFolder = properties.getProperty("sourceFolder");
        String targetFolder = properties.getProperty("targetFolder");
        String prefix = properties.getProperty("addSuffixeImageTarget");

        List<DataToProcess> dataToProcessList = null;
        try {
            dataToProcessList = CSVReader.readDataFromCSV(csvFile, csvFileSeparator, sourceFolder);
        } catch (IOException e) {
            LOGGER.error("Error while reading csv file", e);
            System.exit(-1);
        }

        List<DataToProcess> dataProcessed = Lists.newArrayList();

        for (DataToProcess dataToProcess : dataToProcessList) {
            Path imageSource = Paths.get(sourceFolder + "/" + dataToProcess.getImageTarget());
            LOGGER.info("Starting to process " + imageSource);
            if (Files.exists(imageSource)) {
                Stopwatch imageTimer = Stopwatch.createStarted();
                String fileExtension = com.google.common.io.Files.getFileExtension(imageSource.toString());
                BufferedImage bufferedImage;
                try {
                    bufferedImage = ImageIO.read(imageSource.toFile());

                    Template template = dataToProcess.getTemplate();
                    ////////////////////////////////// Starting Image processing ///////////////////////////////////////////

                    BufferedImage processedImage = ImageUtils.getComputedImage(bufferedImage, template.getWidth()
                            , template.getHeight(), template.getMargin(), 1100, 1200);

                    //////////////////////////////////          End              ///////////////////////////////////////////
                    if (processedImage != null) {
                        String imageTarget;
                        Path imageTargetPath = Paths.get(targetFolder);
                        if (prefix != null) {
                            String[] s = dataToProcess.getUrlImageSource().split("\\.");
                            imageTarget = sourceFolder + "/" + s[0] + prefix + "." + fileExtension;
                        } else {
                            imageTarget = imageTargetPath.toString() + "/" + dataToProcess.getImageTarget();
                        }
                        try {
                            ImageIO.write(processedImage, fileExtension, new File(imageTarget));
                            dataProcessed.add(dataToProcess);
                            LOGGER.info(imageTarget + " was correctly processed in " + imageTimer);
                        } catch (IOException e) {
                            LOGGER.error("Error while writing image " + imageTarget, e);
                        } finally {
                            imageProcessedCount++;
                        }
                    } else {
                        LOGGER.warn("Error while processing image " + dataToProcess.toString());
                    }

                } catch (IOException e) {
                    LOGGER.error("Error while reading image " + imageSource.getFileName(), e);
                }
            } else {
                LOGGER.warn("Image not found");
            }
        }
        timer.stop();
        LOGGER.info(imageProcessedCount + " images were processed in " + timer);
        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        List<String> resultCSVLines = Lists.newArrayList();
        resultCSVLines.add(imageProcessedCount + " images were processed in " + timer);
        for (DataToProcess dtp : dataProcessed) {
            resultCSVLines.add(dtp.getUrlProd() + " | " + dtp.getUrlInteg() + " | " + dtp.getTemplate().toString());
        }
        final Path outputFile = Paths.get(targetFolder, "result-" + timeStamp + ".csv");
        try {
            Files.write(outputFile, resultCSVLines);
        } catch (IOException e) {
            LOGGER.error("Error while writing results file log");
        }


        System.exit(0);
    }


}


/*        Files.newDirectoryStream(Paths.get(CURRENT_PATH.toURI()),"*.{jpg}").forEach((Path dir)->{
            BufferedImage imageBuffered = null;

            try{

                imageBuffered = ImageIO.read(dir.toFile());*/
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
/*                BufferedImage finalBufferedImage =ImageUtils.getComputedImageWithLABClusters(imageBuffered,653,653,178,1100,1200);
                String temp = System.getProperty("java.io.tmpdir");
                File outputFile = new File(temp+"resultat_POC5.jpg");
                LOGGER.info("Writting resulting image to {}",outputFile);
                ImageIO.write(finalBufferedImage,"jpg",outputFile);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }*/
/*        });*/

