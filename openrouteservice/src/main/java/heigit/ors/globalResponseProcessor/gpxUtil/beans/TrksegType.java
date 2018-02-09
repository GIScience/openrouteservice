
package heigit.ors.globalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * A Track Segment holds a list of Track Points which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
 * <p>
 * <p>
 * <p>Java class for trksegType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * {@code
 * <complexType name="trksegType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="trkpt" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "trksegType", propOrder = {
        "trkpt",
        "extensions"
})
public class TrksegType {

    protected List<WptType> trkpt;
    protected TrksegTypeExtensions extensions;

    /**
     * Gets the value of the trkpt property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trkpt property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrkpt().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WptType }
     */
    public List<WptType> getTrkpt() {
        if (trkpt == null) {
            trkpt = new ArrayList<>();
        }
        return this.trkpt;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return possible object is
     * {@link TrksegTypeExtensions }
     */
    public TrksegTypeExtensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is
     *              {@link TrksegTypeExtensions }
     */
    public void setExtensions(TrksegTypeExtensions value) {
        this.extensions = value;
    }

}
