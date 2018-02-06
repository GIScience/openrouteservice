
package heigit.ors.util.GlobalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * 	 A link to an external resource (Web page, digital photo, video clip, etc) with additional information.
 *
 *
 * <p>Java class for linkType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * {@code
 * <complexType name="linkType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="href" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkType", propOrder = {
    "text",
    "type"
})
public class LinkType {

    protected String text;
    protected String type;
    @XmlAttribute(name = "href", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String href;

    /**
     * Gets the value of the text property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the type property.
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
     * Sets the value of the type property.
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
     * Gets the value of the href property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setHref(String value) {
        this.href = value;
    }

}
