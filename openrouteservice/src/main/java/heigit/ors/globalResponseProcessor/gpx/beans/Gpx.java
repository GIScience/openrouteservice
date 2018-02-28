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

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * GPX documents contain a metadata header, followed by waypoints, routes, and tracks.  You can add your own elements
 * to the extensions section of the GPX document.
 * <p>
 * <p>
 * <p>Java class for gpxType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * <complexType name="gpxType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="metadata" type="{http://www.topografix.com/GPX/1/1}metadataType" minOccurs="0"/>
 *         <element name="wpt" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="rte" type="{http://www.topografix.com/GPX/1/1}rteType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="trk" type="{http://www.topografix.com/GPX/1/1}trkType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.1" />
 *       <attribute name="creator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 *
 * @author Julian Psotta, julian@openrouteservice.org
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
     * @return possible object is
     * {@link MetadataType }
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     *
     * @param value allowed object is
     *              {@link MetadataType }
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the wpt property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wpt property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWpt().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WptType }
     */
    public List<WptType> getWpt() {
        if (wpt == null) {
            wpt = new ArrayList<>();
        }
        return this.wpt;
    }

    /**
     * Gets the value of the rte property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rte property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRte().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RteType }
     */
    public List<RteType> getRte() {
        if (rte == null) {
            rte = new ArrayList<>();
        }
        return this.rte;
    }

    /**
     * Gets the value of the trk property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trk property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrk().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrkType }
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
     * @return possible object is
     * {@link GpxExtensions }
     */
    public GpxExtensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is
     *              {@link MetadataTypeExtensions }
     */
    public void setExtensions(GpxExtensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
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
     * @param value allowed object is
     *              {@link String }
     */
    public String setVersion(String value) {
        this.version = value;
        return value;
    }

    /**
     * Gets the value of the creator property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCreator(String value) {
        this.creator = value;
    }

    public String build() throws JAXBException {
        XMLBuilder builder = new XMLBuilder();
        return builder.build(this);
    }
}
