
package heigit.ors.GlobalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * 
 * 	 A geographic point with optional elevation and time.  Available for use by other schemas.
 *
 *
 * <p>Java class for ptType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * {@code
 * <complexType name="ptType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="ele" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="lat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
 *       <attribute name="lon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ptType", propOrder = {
    "ele",
    "time"
})
public class PtType {

    protected BigDecimal ele;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    @XmlAttribute(name = "lat", required = true)
    protected BigDecimal lat;
    @XmlAttribute(name = "lon", required = true)
    protected BigDecimal lon;

    /**
     * Gets the value of the ele property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getEle() {
        return ele;
    }

    /**
     * Sets the value of the ele property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setEle(BigDecimal value) {
        this.ele = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the lat property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLat(BigDecimal value) {
        this.lat = value;
    }

    /**
     * Gets the value of the lon property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLon(BigDecimal value) {
        this.lon = value;
    }

}
