
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * wpt represents a waypoint, point of interest, or named feature on a map.
 * <p>
 * <p>
 * <p>Java class for wptType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * {@code
 * <complexType name="wptType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="ele" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         <element name="magvar" type="{http://www.topografix.com/GPX/1/1}degreesType" minOccurs="0"/>
 *         <element name="geoidheight" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="cmt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="src" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="sym" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="fix" type="{http://www.topografix.com/GPX/1/1}fixType" minOccurs="0"/>
 *         <element name="sat" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         <element name="hdop" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="vdop" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="pdop" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="ageofdgpsdata" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         <element name="dgpsid" type="{http://www.topografix.com/GPX/1/1}dgpsStationType" minOccurs="0"/>
 *         <element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="lat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
 *       <attribute name="lon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
@SuppressWarnings("WeakerAccess")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wptType", propOrder = {
        "ele",
        "time",
        "magvar",
        "geoidheight",
        "name",
        "cmt",
        "desc",
        "src",
        "link",
        "sym",
        "type",
        "fix",
        "sat",
        "hdop",
        "vdop",
        "pdop",
        "ageofdgpsdata",
        "dgpsid",
        "extensions"
})
public class WptType {

    protected BigDecimal ele;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    protected BigDecimal magvar;
    protected BigDecimal geoidheight;
    protected String name;
    protected String cmt;
    protected String desc;
    protected String src;
    protected List<LinkType> link;
    protected String sym;
    protected String type;
    protected String fix;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger sat;
    protected BigDecimal hdop;
    protected BigDecimal vdop;
    protected BigDecimal pdop;
    protected BigDecimal ageofdgpsdata;
    @XmlSchemaType(name = "integer")
    protected Integer dgpsid;
    protected WptTypeExtensions extensions;
    @XmlAttribute(name = "lat", required = true)
    protected BigDecimal lat;
    @XmlAttribute(name = "lon", required = true)
    protected BigDecimal lon;

    /**
     * Gets the value of the ele property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getEle() {
        return ele;
    }

    /**
     * Sets the value of the ele property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setEle(BigDecimal value) {
        this.ele = value;
    }

    /**
     * Gets the value of the time property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the magvar property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getMagvar() {
        return magvar;
    }

    /**
     * Sets the value of the magvar property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setMagvar(BigDecimal value) {
        this.magvar = value;
    }

    /**
     * Gets the value of the geoidheight property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getGeoidheight() {
        return geoidheight;
    }

    /**
     * Sets the value of the geoidheight property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setGeoidheight(BigDecimal value) {
        this.geoidheight = value;
    }

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
     * Gets the value of the sym property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSym() {
        return sym;
    }

    /**
     * Sets the value of the sym property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSym(String value) {
        this.sym = value;
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
     * Gets the value of the fix property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFix() {
        return fix;
    }

    /**
     * Sets the value of the fix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFix(String value) {
        this.fix = value;
    }

    /**
     * Gets the value of the sat property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getSat() {
        return sat;
    }

    /**
     * Sets the value of the sat property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setSat(BigInteger value) {
        this.sat = value;
    }

    /**
     * Gets the value of the hdop property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getHdop() {
        return hdop;
    }

    /**
     * Sets the value of the hdop property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setHdop(BigDecimal value) {
        this.hdop = value;
    }

    /**
     * Gets the value of the vdop property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getVdop() {
        return vdop;
    }

    /**
     * Sets the value of the vdop property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setVdop(BigDecimal value) {
        this.vdop = value;
    }

    /**
     * Gets the value of the pdop property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getPdop() {
        return pdop;
    }

    /**
     * Sets the value of the pdop property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setPdop(BigDecimal value) {
        this.pdop = value;
    }

    /**
     * Gets the value of the ageofdgpsdata property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getAgeofdgpsdata() {
        return ageofdgpsdata;
    }

    /**
     * Sets the value of the ageofdgpsdata property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setAgeofdgpsdata(BigDecimal value) {
        this.ageofdgpsdata = value;
    }

    /**
     * Gets the value of the dgpsid property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getDgpsid() {
        return dgpsid;
    }

    /**
     * Sets the value of the dgpsid property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setDgpsid(Integer value) {
        this.dgpsid = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return possible object is
     * {@link WptTypeExtensions }
     */
    public WptTypeExtensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is
     *              {@link WptTypeExtensions }
     */
    public void setExtensions(WptTypeExtensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the lat property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setLat(BigDecimal value) {
        this.lat = value;
    }

    /**
     * Gets the value of the lon property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setLon(BigDecimal value) {
        this.lon = value;
    }

}
