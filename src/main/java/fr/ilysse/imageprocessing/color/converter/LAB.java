package fr.ilysse.imageprocessing.color.converter;

import sun.nio.cs.ext.MacHebrew;

import java.awt.*;

/**
 * Created by p_poucif on 03/11/2016.
 */
public class LAB {

    // D65 standart referent
    private static final float X = 0.950470F;
    private static final float Y = 1.0F;
    private static final float Z = 1.088830F;

    private float L;
    private float A;
    private float B;
    private Integer clusterIndex = null;

    public LAB(){
        this.L = 0;
        this.A = 0;
        this.B = 0;
    }

    public LAB(Color RGBColor){
        float[] LAB = RGBtoLAB(RGBColor);
        this.L = LAB[0];
        this.A = LAB[1];
        this.B = LAB[2];
    }

    private float[] RGBtoLAB(Color RGBColor){

        float vR = RGBColor.getRed()/255F;
        float vG = RGBColor.getGreen()/255F;
        float vB = RGBColor.getBlue()/255F;

        vR = vR > 0.04045F ? (float)Math.pow((vR+0.055F)/1.055F,2.4) : vR / 12.95F;
        vG = vG > 0.04045F ? (float)Math.pow((vG+0.055F)/1.055F,2.4) : vG / 12.95F;
        vB = vB > 0.04045F ? (float)Math.pow((vB+0.055F)/1.055F,2.4) : vB / 12.95F;

        vR *=100F;
        vG *=100F;
        vB *=100F;

        // RGB to XYZ
        float x = (0.4124564F*vR+0.3575761F*vG+0.1804375F*vB)/X;
        float y = (0.2126729F*vR+0.7151522F*vG+0.0721750F*vB)/Y;
        float z = (0.0193339F*vR+0.1191920F*vG+0.9503041F*vB)/Z;

        x = x > 0.008856F ? (float)Math.pow(x,1.0F/3) : 7.787037F*x + 4F/29;
        y = y > 0.008856F ? (float)Math.pow(y,1.0F/3) : 7.787037F*y + 4F/29;
        z = z > 0.008856F ? (float)Math.pow(z,1.0F/3) : 7.787037F*z + 4F/29;

        float[] LAB = new float[3];
        LAB[0]=116*y-16;
        LAB[1]=500*(x-y);
        LAB[2]=200*(y-z);

        return LAB;

    }

    public float getL() {
        return L;
    }

    public void setL(float l) {
        L = l;
    }

    public float getA() {
        return A;
    }

    public void setA(float a) {
        A = a;
    }

    public float getB() {
        return B;
    }

    public void setB(float b) {
        B = b;
    }

    public float[] getAB(){
        float[] AB = new float[2];
        AB[0] = A;
        AB[1] = B;
        return AB;
    }

    public void setClusterIndex(Integer clusterIndex){
            this.clusterIndex=clusterIndex;
    }

    public Integer getClusterIndex(){ return clusterIndex;}

}
