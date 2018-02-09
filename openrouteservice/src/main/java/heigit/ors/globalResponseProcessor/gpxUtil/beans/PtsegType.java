
package heigit.ors.globalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * An ordered sequence of points.  (for polygons or polylines, e.g.)
 * <p>
 * <p>
 * <p>Java class for ptsegType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * {@code
 * <complexType name="ptsegType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="pt" type="{http://www.topografix.com/GPX/1/1}ptType" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
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
@XmlType(name = "ptsegType", propOrder = {
        "pt"
})
public class PtsegType {

    protected List<PtType> pt;

    /**
     * Gets the value of the pt property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pt property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPt().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PtType }
     */
    public List<PtType> getPt() {
        if (pt == null) {
            pt = new ArrayList<>();
        }
        return this.pt;
    }

}
