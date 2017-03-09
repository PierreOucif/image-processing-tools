package fr.ilysse.imageprocessing;

import com.google.common.base.Stopwatch;
import fr.ilysse.imageprocessing.image.ImageUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by p_poucif on 09/03/2017.
 */
public class DetourageApp {

    public static final Logger LOGGER = LoggerFactory.getLogger(DetourageApp.class);
    public static final String CONFIG_URL = "resources/detourage/config.properties";

    public static void main(String[] args) {

        final Stopwatch totalTimer = Stopwatch.createStarted();
        final Path configPath = Paths.get(CONFIG_URL);

        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(configPath));

            final String sourceFolder = properties.getProperty("sourceFolder");
            final String targetFolder = properties.getProperty("targetFolder");

            if (StringUtils.isNotEmpty(sourceFolder) && StringUtils.isNotEmpty(targetFolder)) {

                final Path sourceFolderPath = Paths.get(sourceFolder);
                if (Files.exists(sourceFolderPath)) {

                    final File[] files = (new File(sourceFolderPath.toUri())).listFiles();

                    for(final File file : files){

                        BufferedImage bufferedImage = ImageIO.read(file);
                        if(bufferedImage!=null){
                            String imageName = file.getName();

                            /////////////////////// Starting image processing ///////////////////////////////////
                            Stopwatch timer = Stopwatch.createStarted();



                            LOGGER.info(imageName+" was correctly processed in "+timer);
                            ////////////////////// End of the image processing //////////////////////////////////

                        }else{
                            LOGGER.warn("Error while reading file "+file+" . It might be not an image !");
                        }

                    }
                }

            } else {
                LOGGER.error("Empty source or traget folder. Check config.properties !");
                System.exit(-1);
            }

        } catch (IOException e) {
            LOGGER.error("Error while searching for config.properties in " + configPath, e);
            System.exit(-1);
        }finally {
            LOGGER.info("DetourageApp main() ran during "+totalTimer);
            System.exit(0);
        }

    }
}
