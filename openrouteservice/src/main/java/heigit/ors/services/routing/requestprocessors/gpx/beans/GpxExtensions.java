package heigit.ors.services.routing.requestprocessors.gpx.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link GpxExtensions} represents the extensions for {@link Gpx}
 * Can be manually extended
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "example1"
        // always add new variables here! and below
})

public class GpxExtensions extends ExtensionsType{

    protected double example1;

    public double getExample1() {
        return example1;
    }

    public void setExample1(double example1) {
        this.example1 = example1;
    }
}
