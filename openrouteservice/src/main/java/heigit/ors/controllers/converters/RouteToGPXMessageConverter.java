package heigit.ors.controllers.converters;

import heigit.ors.controllers.responseHolders.GPXRouteResponse;
import heigit.ors.controllers.responseHolders.Route;
import org.json.XML;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
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

public class RouteToGPXMessageConverter extends AbstractHttpMessageConverter<GPXRouteResponse> {
    public RouteToGPXMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> inClass) {
        if(inClass.isAssignableFrom(GPXRouteResponse.class))
            return true;
        else
            return false;
    }

    @Override
    protected GPXRouteResponse readInternal(Class<? extends GPXRouteResponse> arg0, HttpInputMessage arg1)
            throws IOException, HttpMessageNotReadableException
    {

        return new GPXRouteResponse();

    }

    @Override
    protected void writeInternal(GPXRouteResponse routeObject, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException
    {
        outputMessage.getHeaders().setContentType(MediaType.APPLICATION_XML);

        Route route = routeObject.getRoute();

        DocumentBuilderFactory xmlBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = xmlBuilderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootEl = doc.createElement("Route");
            doc.appendChild(rootEl);

            Element startEl = doc.createElement("Start");
            startEl.appendChild(doc.createTextNode(route.getStartPoint().toString()));
            rootEl.appendChild(startEl);

            Element endEl = doc.createElement("Destination");
            endEl.appendChild(doc.createTextNode(route.getDestinationPoint().toString()));
            rootEl.appendChild(endEl);

            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));


            outputMessage.getBody().write(sw.toString().getBytes());
        } catch (ParserConfigurationException pce) {

        } catch (TransformerConfigurationException tce) {

        } catch (TransformerException te) {

        }
    }
}
