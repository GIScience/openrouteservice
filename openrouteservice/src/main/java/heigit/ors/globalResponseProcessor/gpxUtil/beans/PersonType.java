
package heigit.ors.globalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * A person or organization.
 * <p>
 * <p>
 * <p>Java class for personType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * {@code
 * <complexType name="personType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="email" type="{http://www.topografix.com/GPX/1/1}emailType" minOccurs="0"/>
 *         <element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" minOccurs="0"/>
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
@XmlType(name = "personType", propOrder = {
        "name",
        "email",
        "link"
})
public class PersonType {

    protected String name;
    protected EmailType email;
    protected LinkType link;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the email property.
     *
     * @return possible object is
     * {@link EmailType }
     */
    public EmailType getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     *
     * @param value allowed object is
     *              {@link EmailType }
     */
    public void setEmail(EmailType value) {
        this.email = value;
    }

    /**
     * Gets the value of the link property.
     *
     * @return possible object is
     * {@link LinkType }
     */
    public LinkType getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     *
     * @param value allowed object is
     *              {@link LinkType }
     */
    public void setLink(LinkType value) {
        this.link = value;
    }

}
