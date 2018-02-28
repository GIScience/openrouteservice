
/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package heigit.ors.globalResponseProcessor.gpx.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * rte represents route - an ordered list of waypoints representing a series of turn points leading to a destination.
 * <p>
 * <p>
 * <p>Java class for rteType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * {@code
 * <complexType name="rteType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="cmt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="src" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="number" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         <element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *         <element name="rtept" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "rteType", propOrder = {
        "name",
        "cmt",
        "desc",
        "src",
        "link",
        "number",
        "type",
        "rtept",
        "extensions"
})
public class RteType {

    protected String name;
    protected String cmt;
    protected String desc;
    protected String src;
    protected List<LinkType> link;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger number;
    protected String type;
    protected RteTypeExtensions extensions;
    protected List<WptType> rtept;

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
     * Gets the value of the cmt property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCmt() {
        return cmt;
    }

    /**
     * Sets the value of the cmt property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmt(String value) {
        this.cmt = value;
    }

    /**
     * Gets the value of the desc property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Gets the value of the src property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of the src property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSrc(String value) {
        this.src = value;
    }

    /**
     * Gets the value of the link property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLink().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkType }
     */
    public List<LinkType> getLink() {
        if (link == null) {
            link = new ArrayList<>();
        }
        return this.link;
    }

    /**
     * Gets the value of the number property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setNumber(BigInteger value) {
        this.number = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return possible object is
     * {@link RteTypeExtensions }
     */
    public RteTypeExtensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is
     *              {@link RteTypeExtensions }
     */
    public void setExtensions(RteTypeExtensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the rtept property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rtept property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRtept().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WptType }
     */
    public List<WptType> getRtept() {
        if (rtept == null) {
            rtept = new ArrayList<>();
        }
        return this.rtept;
    }

}
