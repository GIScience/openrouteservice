package heigit.ors.util.gpxUtil;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * 		GPX documents contain a metadata header, followed by waypoints, routes, and tracks.  You can add your own elements
 * 		to the extensions section of the GPX document.
 *
 *
 * <p>Java class for gpxType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="gpxType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metadata" type="{http://www.topografix.com/GPX/1/1}metadataType" minOccurs="0"/>
 *         &lt;element name="wpt" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rte" type="{http://www.topografix.com/GPX/1/1}rteType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="trk" type="{http://www.topografix.com/GPX/1/1}trkType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.1" />
 *       &lt;attribute name="creator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gpxType", propOrder = {
        "metadata",
        "wpt",
        "rte",
        "trk",
        "extensions"
})
public class Gpx {

    protected MetadataType metadata;
    protected List<WptType> wpt;
    protected List<RteType> rte;
    protected List<TrkType> trk;
    protected GpxExtensions extensions;
    @XmlAttribute(name = "version", required = true)
    // gpx version is better to be hardcoded
    protected String version = this.setVersion("1.0");
    @XmlAttribute(name = "creator", required = true)
    protected String creator;

    /**
     * Gets the value of the metadata property.
     *
     * @return
     *     possible object is
     *     {@link MetadataType }
     *     
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     *
     * @param value
     *     allowed object is
     *     {@link MetadataType }
     *     
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the wpt property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wpt property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWpt().add(newItem);
     * </pre>
     *
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WptType }
     *
     * 
     */
    public List<WptType> getWpt() {
        if (wpt == null) {
            wpt = new ArrayList<>();
        }
        return this.wpt;
    }

    /**
     * Gets the value of the rte property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rte property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRte().add(newItem);
     * </pre>
     *
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RteType }
     *
     * 
     */
    public List<RteType> getRte() {
        if (rte == null) {
            rte = new ArrayList<>();
        }
        return this.rte;
    }

    /**
     * Gets the value of the trk property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trk property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrk().add(newItem);
     * </pre>
     *
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrkType }
     *
     * 
     */
    public List<TrkType> getTrk() {
        if (trk == null) {
            trk = new ArrayList<>();
        }
        return this.trk;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return
     *     possible object is
     *     {@link GpxExtensions }
     *     
     */
    public GpxExtensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value
     *     allowed object is
     *     {@link MetadataTypeExtensions }
     *     
     */
    public void setExtensions(GpxExtensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1.1";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public String setVersion(String value) {
        this.version = value;
        return value;
    }

    /**
     * Gets the value of the creator property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreator(String value) {
        this.creator = value;
    }

    public String build() throws JAXBException {
        heigit.ors.util.gpxUtil.XMLBuilder builder = new heigit.ors.util.gpxUtil.XMLBuilder();
        return builder.Build(this);
    }
}
