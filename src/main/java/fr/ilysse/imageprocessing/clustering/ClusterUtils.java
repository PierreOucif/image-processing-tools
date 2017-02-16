package fr.ilysse.imageprocessing.clustering;

import com.google.common.base.Stopwatch;
import fr.ilysse.imageprocessing.color.converter.LAB;
import fr.ilysse.imageprocessing.image.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by p_poucif on 03/11/2016.
 */
public class ClusterUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterUtils.class);
    private static final double DELTA_WHITE = 0.005;
    private static final LAB WHITE_LAB = new LAB(Color.WHITE);
    private static final Integer WHITE_CLUSTER=0;

    public List<LAB> executeKMeans(int nbCluster,int nbIteration,List<LAB> listOfLABs,boolean displayClusters){
        LOGGER.info("Starting kMeans computation for K = {} and nbIteration = {} for {} LABs",nbCluster,nbIteration,listOfLABs.size());
        List<LAB> meanLABs = initialization(nbCluster,listOfLABs);

        for(int i=0;i<nbIteration;i++) {
            LOGGER.info("Starting iteration #{}",i);
            meanLABs  = calculateNewLABClusters(listOfLABs, meanLABs);
            if(displayClusters) {
                ImageUtils.displayLAB(listOfLABs);
            }
            LOGGER.info("Ending iteration #{}",i);
        }

        return listOfLABs;
    }


    private List<LAB> initialization(int nbCluster,List<LAB> dataToClustered){
        LOGGER.info("Initialization of {} random clusters ...",nbCluster);
        Stopwatch timer = Stopwatch.createUnstarted();
        List<LAB> meanLABs = new ArrayList<>();
        // Datas initialization => random clusters
        timer.start();


        for(int i=1; i<=nbCluster;i++){
            LAB meanLAB = new LAB();
            meanLAB.setClusterIndex(i);
            int jMin = Math.round((i-1)*dataToClustered.size()/nbCluster);
            int jMax = Math.round(i*dataToClustered.size()/nbCluster);
            int jWhite = 0;
            for(int j = jMin;j<jMax;j++) {
                if(isWhite(dataToClustered.get(j))){
                    dataToClustered.get(j).setClusterIndex(WHITE_CLUSTER);
                    jWhite++;
                }else {
                    dataToClustered.get(j).setClusterIndex(i);
                    meanLAB.setA(dataToClustered.get(j).getA() + meanLAB.getA());
                    meanLAB.setB(dataToClustered.get(j).getB() + meanLAB.getB());
                }
            }
            meanLAB.setA(meanLAB.getA()/(jMax-jWhite+1));
            meanLAB.setB(meanLAB.getB()/(jMax-jWhite+1));
            meanLABs.add(meanLAB);
            LOGGER.info("Cluster #{} from {} to {} : a_mean = {} / b_mean = {}",i,jMin,jMax,meanLAB.getA(),meanLAB.getB());
        }
        timer.stop();
        LOGGER.info("{} random clusters were succefully created in {}",meanLABs.size(),timer);

        return meanLABs;
    }


    public Integer getCloserCentroid(LAB lab, List<LAB> meanLABs){
        double delta = getDeltaCentroid(lab,meanLABs.get(0));
        int closerCentroid = 0;

        for(int i=0;i<meanLABs.size();i++){
            double iDelta = getDeltaCentroid(lab,meanLABs.get(i));
            if(Double.compare(delta,iDelta)>0){
                delta=iDelta;
                closerCentroid=i;
            }
        }

        return closerCentroid;
    }

    public double getDeltaCentroid(LAB lab, LAB meanLAB){
        double xSquare = Math.pow(lab.getA()-meanLAB.getA(),2);
        double ySquare = Math.pow(lab.getB()-meanLAB.getB(),2);
        return Math.sqrt(xSquare+ySquare);
    }

    public List<LAB> calculateNewLABClusters(List<LAB> listOfLABs, List<LAB> meanLABs){
        LOGGER.info("Starting to get {} new clusters of {} LABs",meanLABs.size(),listOfLABs.size());

        LOGGER.info("Initialization of the new list of {} mean LABs",meanLABs.size());
        List<LAB> newMeanLABs = new ArrayList<>();
        List<Cluster> clusters = new ArrayList<>();
        for (int i =0;i<meanLABs.size();i++){
            clusters.add(new Cluster());
        }
        LOGGER.info("The new list of {} mean LABs was well initialized",clusters.size());

        for(LAB lab : listOfLABs) {
            Integer closerCentroidIndex = getCloserCentroid(lab,meanLABs);
            clusters.get(closerCentroidIndex).add(lab);
            lab.setClusterIndex(getCloserCentroid(lab, meanLABs));
        }


        for(int i=0;i<clusters.size();i++){
            LAB meanLAB = clusters.get(i).getMeanLAB();
            meanLAB.setClusterIndex(i);
            newMeanLABs.add(meanLAB);
            LOGGER.info("Mean LAB(a = {} ;b = {}) added to cluster #{} containing {} LABs",meanLAB.getA(),meanLAB.getB(),i,clusters.get(i).getSize());
        }

        LOGGER.info("Got {} newMeanLABs",newMeanLABs.size());
        return newMeanLABs;
    }

    private boolean isWhite(LAB lab){
        if(Double.compare(getDeltaCentroid(lab,WHITE_LAB),DELTA_WHITE)>0){
            return false;
        }else{
            return true;
        }
    }


}
