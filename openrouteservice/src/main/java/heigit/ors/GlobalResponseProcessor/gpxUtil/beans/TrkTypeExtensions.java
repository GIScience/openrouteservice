package heigit.ors.GlobalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link TrkTypeExtensions} represents the extensions for {@link TrkType}
 * Can be manually extended
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "example1"
        // always add new variables here! and below
})

public class TrkTypeExtensions extends ExtensionsType {

    protected double example1;

    public double getExample1() {
        return example1;
    }

    public void setExample1(double example1) {
        this.example1 = example1;
    }
}
