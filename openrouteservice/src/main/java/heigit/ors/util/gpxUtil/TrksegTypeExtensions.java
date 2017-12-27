package heigit.ors.util.gpxUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link TrksegTypeExtensions} represents the extensions for {@link TrksegType}
 * Can be manually extended
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "example1"
        // always add new variables here! and below
})

public class TrksegTypeExtensions extends ExtensionsType {

    protected double example1;

    public double getExample1() {
        return example1;
    }

    public void setExample1(double example1) {
        this.example1 = example1;
    }
}
