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
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;


/**
 * Two lat/lon pairs defining the extent of an element.
 * <p>
 * <p>
 * <p>Java class for boundsType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * {@code
 * <complexType name="boundsType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="minlat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
 *       <attribute name="minlon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
 *       <attribute name="maxlat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
 *       <attribute name="maxlon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "boundsType")
public class BoundsType {

    @XmlAttribute(name = "minlat", required = true)
    protected BigDecimal minlat;
    @XmlAttribute(name = "minlon", required = true)
    protected BigDecimal minlon;
    @XmlAttribute(name = "maxlat", required = true)
    protected BigDecimal maxlat;
    @XmlAttribute(name = "maxlon", required = true)
    protected BigDecimal maxlon;

    /**
     * Gets the value of the minlat property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getMinlat() {
        return minlat;
    }

    /**
     * Sets the value of the minlat property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setMinlat(BigDecimal value) {
        this.minlat = value;
    }

    /**
     * Gets the value of the minlon property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getMinlon() {
        return minlon;
    }

    /**
     * Sets the value of the minlon property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setMinlon(BigDecimal value) {
        this.minlon = value;
    }

    /**
     * Gets the value of the maxlat property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getMaxlat() {
        return maxlat;
    }

    /**
     * Sets the value of the maxlat property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setMaxlat(BigDecimal value) {
        this.maxlat = value;
    }

    /**
     * Gets the value of the maxlon property.
     *
     * @return possible object is
     * {@link BigDecimal }
     */
    public BigDecimal getMaxlon() {
        return maxlon;
    }

    /**
     * Sets the value of the maxlon property.
     *
     * @param value allowed object is
     *              {@link BigDecimal }
     */
    public void setMaxlon(BigDecimal value) {
        this.maxlon = value;
    }

}
