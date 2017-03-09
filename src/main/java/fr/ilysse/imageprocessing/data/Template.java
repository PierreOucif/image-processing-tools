package fr.ilysse.imageprocessing.data;

/**
 * Created by p_poucif on 23/02/2017.
 */
public enum Template {
    F1("F1", "TEMPLATE-01", "Flottant", 460, 460, 0),
    F2("F2", "TEMPLATE-02", "Flottant", 460, 593, 0),
    F3("F3", "TEMPLATE-03", "Flottant", 653, 653, 0),
    F4("F4", "TEMPLATE-04", "Flottant", 576, 743, 0),
    F5("F5", "TEMPLATE-05", "Flottant", 653, 843, 0),
    F6("F6", "TEMPLATE-06", "Flottant", 653, 944, 0),
    F7("F7", "TEMPLATE-07", "Flottant", 808, 1043, 0),
    F8("F8", "TEMPLATE-08", "Flottant", 810, 520, 0),
    P1("P1", "TEMPLATE-09", "Posé", 460, 460, 0),
    P2("P2", "TEMPLATE-10", "Posé", 460, 593, 0),
    P3("P3", "TEMPLATE-11", "Posé", 653, 653, 0),
    P4("P4", "TEMPLATE-12", "Posé", 653, 843, 0),
    P5("P5", "TEMPLATE-13", "Posé", 853, 944, 178),
    P6("P6", "TEMPLATE-14", "Posé", 810, 520, 178),
    P7("P7", "TEMPLATE-15", "Posé", 753, 972, 178),
    SP1("SP1", "TEMPLATE-16", "Spécial", 1100, 1023, 178),
    SP2("SP2", "TEMPLATE-17", "Spécial", 1100, 1023, -178),
    SP3("SP3", "TEMPLATE-18", "Spécial", 1100, 1200, 0),
    F9("F9", "TEMPLATE-19", "Flottant", 1000, 1100, 0);

    final String key;
    final String code;
    final String type;
    final Integer width;
    final Integer height;
    final Integer margin;

    Template(final String key, final String code, final String type, final Integer width, final Integer height, final Integer margin) {
        this.key = key;
        this.code = code;
        this.type = type;
        this.width = width;
        this.height = height;
        this.margin = margin;
    }

    public String toString() {
        return this.type + " " + this.code + " " + this.key + " " + this.width + " px * " + this.height + " px, margin = " + this.margin + " px";
    }


    public String getKey() {
        return key;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getMargin() {
        return margin;
    }
}
