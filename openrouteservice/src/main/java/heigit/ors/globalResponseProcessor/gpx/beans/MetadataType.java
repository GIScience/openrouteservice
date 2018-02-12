
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
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


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
    @SuppressWarnings("WeakerAccess")
    protected CopyrightType copyright;
    protected List<LinkType> link;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    protected String keywords;
    protected BoundsType bounds;
    @SuppressWarnings("WeakerAccess")
    protected MetadataTypeExtensions extensions;

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
     * Gets the value of the author property.
     *
     * @return possible object is
     * {@link PersonType }
     */
    public PersonType getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     *
     * @param value allowed object is
     *              {@link PersonType }
     */
    public void setAuthor(PersonType value) {
        this.author = value;
    }

    /**
     * Gets the value of the copyright property.
     *
     * @return possible object is
     * {@link CopyrightType }
     */
    public CopyrightType getCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     *
     * @param value allowed object is
     *              {@link CopyrightType }
     */
    public void setCopyright(CopyrightType value) {
        this.copyright = value;
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
     * Sets the link value
     *
     * @param link needs a List<LinkType> as input
     */
    public void setLink(List<LinkType> link) {
        this.link = link;
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
     * Gets the value of the keywords property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the value of the keywords property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeywords(String value) {
        this.keywords = value;
    }

    /**
     * Gets the value of the bounds property.
     *
     * @return possible object is
     * {@link BoundsType }
     */
    public BoundsType getBounds() {
        return bounds;
    }

    /**
     * Sets the value of the bounds property.
     *
     * @param value allowed object is
     *              {@link BoundsType }
     */
    public void setBounds(BoundsType value) {
        this.bounds = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return possible object is
     * {@link MetadataTypeExtensions }
     */
    public MetadataTypeExtensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is
     *              {@link MetadataTypeExtensions }
     */
    public void setExtensions(MetadataTypeExtensions value) {
        this.extensions = value;
    }

}
