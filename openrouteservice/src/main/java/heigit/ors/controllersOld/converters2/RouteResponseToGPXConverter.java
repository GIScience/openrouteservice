package heigit.ors.controllersOld.converters2;

import heigit.ors.controllersOld.DataTransferObjects2.RouteObjectDTO;
import heigit.ors.controllersOld.DataTransferObjects2.RouteResponseDTO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class RouteResponseToGPXConverter implements RouteResponseMessageConverter {
    public String generateResponseBody(RouteResponseDTO routeObject)
            throws IOException
    {
        ArrayList<RouteObjectDTO> routes = routeObject.getRoutes();

        String responseBody = "";

        DocumentBuilderFactory xmlBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = xmlBuilderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootEl = doc.createElement("Routes");
            doc.appendChild(rootEl);

            for(RouteObjectDTO route : routes) {
                Element routeEl = doc.createElement("Route");
                routeEl.appendChild(doc.createTextNode("test"));
                rootEl.appendChild(routeEl);
            }

            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));


            responseBody = sw.toString();
        } catch (ParserConfigurationException pce) {

        } catch (TransformerConfigurationException tce) {

        } catch (TransformerException te) {

        }

        return responseBody;
    }
}
