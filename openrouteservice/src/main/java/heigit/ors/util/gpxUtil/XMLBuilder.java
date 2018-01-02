package heigit.ors.util.gpxUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * @author Julian
 * This classs generates the xml representation of the gpx file as a formatted string.
 * The JAXB Marshaller goes recursively through all the classes.
 */
class XMLBuilder {

    /**
     * {@link XMLBuilder} functions as an empty placeholder class.
     */
    XMLBuilder() {

    }

    /**
     * The function creates a XML Element from a GPX and returns it as a string representation.
     *
     * @param gpx Needs a gpx as an Input.
     * @return Returns the GPX as a well formatted XML
     * @throws JAXBException Throws {@link JAXBException} exception in case of failure
     */

    public String Build(Gpx gpx) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Gpx.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        m.marshal(gpx, sw);
        return sw.toString();
    }
}
