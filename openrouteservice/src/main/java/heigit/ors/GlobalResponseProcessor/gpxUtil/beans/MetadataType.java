
package heigit.ors.GlobalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 		Information about the GPX file, author, and copyright restrictions goes in the metadata section.  Providing rich,
 * 		meaningful information about your GPX files allows others to search for and use your GPS data.
 *
 *
 * <p>Java class for metadataType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * {@code
 * <complexType name="metadataType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="author" type="{http://www.topografix.com/GPX/1/1}personType" minOccurs="0"/>
 *         <element name="copyright" type="{http://www.topografix.com/GPX/1/1}copyrightType" minOccurs="0"/>
 *         <element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         <element name="keywords" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="bounds" type="{http://www.topografix.com/GPX/1/1}boundsType" minOccurs="0"/>
 *         <element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metadataType", propOrder = {
    "name",
    "desc",
    "author",
    "copyright",
    "link",
    "time",
    "keywords",
    "bounds",
    "extensions"
})
public class MetadataType {

    protected String name;
    protected String desc;
    protected PersonType author;
    protected CopyrightType copyright;
    protected List<LinkType> link;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    protected String keywords;
    protected BoundsType bounds;
    protected MetadataTypeExtensions extensions;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link PersonType }
     *     
     */
    public PersonType getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link PersonType }
     *     
     */
    public void setAuthor(PersonType value) {
        this.author = value;
    }

    /**
     * Gets the value of the copyright property.
     * 
     * @return
     *     possible object is
     *     {@link CopyrightType }
     *     
     */
    public CopyrightType getCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     * 
     * @param value
     *     allowed object is
     *     {@link CopyrightType }
     *     
     */
    public void setCopyright(CopyrightType value) {
        this.copyright = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLink().add(newItem);
     * </pre>
     *
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkType }
     *
     * 
     */
    public List<LinkType> getLink() {
        if (link == null) {
            link = new ArrayList<>();
        }
        return this.link;
    }

    /**
     * Sets the link value
     * @param link needs a List<LinkType> as input
     */
    public void setLink(List<LinkType> link) {
        this.link = link;
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
     * Gets the value of the keywords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the value of the keywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeywords(String value) {
        this.keywords = value;
    }

    /**
     * Gets the value of the bounds property.
     * 
     * @return
     *     possible object is
     *     {@link BoundsType }
     *     
     */
    public BoundsType getBounds() {
        return bounds;
    }

    /**
     * Sets the value of the bounds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BoundsType }
     *     
     */
    public void setBounds(BoundsType value) {
        this.bounds = value;
    }

    /**
     * Gets the value of the extensions property.
     * 
     * @return
     *     possible object is
     *     {@link MetadataTypeExtensions }
     *     
     */
    public MetadataTypeExtensions getExtensions() {
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
    public void setExtensions(MetadataTypeExtensions value) {
        this.extensions = value;
    }

}
