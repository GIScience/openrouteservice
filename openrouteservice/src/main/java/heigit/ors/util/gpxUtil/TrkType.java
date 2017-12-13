//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.11.29 um 04:11:24 PM CET 
//


package heigit.ors.util.gpxUtil;

import heigit.ors.util.gpxUtil.LinkType;
import heigit.ors.util.gpxUtil.TrkTypeExtensions;
import heigit.ors.util.gpxUtil.TrksegType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 		trk represents a track - an ordered list of points describing a path.
 * 	  
 * 
 * <p>Java-Klasse f�r trkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="trkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cmt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="src" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *         &lt;element name="trkseg" type="{http://www.topografix.com/GPX/1/1}trksegType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "trkType", propOrder = {
    "name",
    "cmt",
    "desc",
    "src",
    "link",
    "number",
    "type",
    "extensions",
    "trkseg"
})
public class TrkType {

    protected String name;
    protected String cmt;
    protected String desc;
    protected String src;
    protected List<LinkType> link;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger number;
    protected String type;
    protected TrkTypeExtensions extensions;
    protected List<TrksegType> trkseg;

    /**
     * Ruft den Wert der name-Eigenschaft ab.
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
     * Legt den Wert der name-Eigenschaft fest.
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
     * Ruft den Wert der cmt-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCmt() {
        return cmt;
    }

    /**
     * Legt den Wert der cmt-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCmt(String value) {
        this.cmt = value;
    }

    /**
     * Ruft den Wert der desc-Eigenschaft ab.
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
     * Legt den Wert der desc-Eigenschaft fest.
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
     * Ruft den Wert der src-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSrc() {
        return src;
    }

    /**
     * Legt den Wert der src-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSrc(String value) {
        this.src = value;
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
     * Ruft den Wert der number-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getNumber() {
        return number;
    }

    /**
     * Legt den Wert der number-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setNumber(BigInteger value) {
        this.number = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der extensions-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link TrkTypeExtensions }
     *
     */
    public TrkTypeExtensions getExtensions() {
        return extensions;
    }

    /**
     * Legt den Wert der extensions-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link TrkTypeExtensions }
     *     
     */
    public void setExtensions(TrkTypeExtensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the trkseg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trkseg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrkseg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrksegType }
     * 
     * 
     */
    public List<TrksegType> getTrkseg() {
        if (trkseg == null) {
            trkseg = new ArrayList<>();
        }
        return this.trkseg;
    }

}
