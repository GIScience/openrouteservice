package heigit.ors.GlobalResponseProcessor.gpxUtil.beans;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link ExtensionsType} provides the superclass for all of the extension classes.
 * More can be added manually but should also be integrated in all of the JAXB parts.
 * This file could hold some general variables. Not needed for now!
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extensions", propOrder = {
        // always add new variables here! and below
})
@XmlSeeAlso({WptTypeExtensions.class, TrkTypeExtensions.class, TrksegTypeExtensions.class, RteTypeExtensions.class, MetadataTypeExtensions.class, GpxExtensions.class})
public class ExtensionsType {

}


