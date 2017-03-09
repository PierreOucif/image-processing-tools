package fr.ilysse.imageprocessing.data;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by p_poucif on 23/02/2017.
 */
public class DataToProcess {

    private String imageTarget;
    private String urlImageSource;
    private Template template;
    private static final String ROOT_URL_PROD = "http://static.galerieslafayette.com/";
    private static final String ROOT_URL_INTEG = "http://static.int-gl.com/media/image_processing_result/";
    private String urlProd;
    private String urlInteg;

    public DataToProcess(String imageTarget, String urlImageSource, Template template) {
        this.imageTarget = imageTarget;
        this.urlImageSource = createSourceUrl(urlImageSource);
        this.template = template;
        this.urlProd = ROOT_URL_PROD + this.urlImageSource;
        this.urlInteg = ROOT_URL_INTEG + this.imageTarget;
    }

    private String createSourceUrl(String urlWithRoot) {
        if (urlWithRoot.contains(ROOT_URL_PROD)) {
            return StringUtils.removeStart(urlWithRoot, ROOT_URL_PROD);
        } else {
            return urlWithRoot;
        }
    }

    public String getImageTarget() {
        return imageTarget;
    }

    public String getUrlImageSource() {
        return urlImageSource;
    }

    public Template getTemplate() {
        return template;
    }

    public String getUrlProd() {
        return urlProd;
    }

    public String getUrlInteg() {
        return urlInteg;
    }

    @Override
    public String toString() {
        return "DataToProcess{" +
                "imageTarget='" + imageTarget + '\'' +
                ", urlImageSource='" + urlImageSource + '\'' +
                ", template=" + template +
                '}';
    }
}
