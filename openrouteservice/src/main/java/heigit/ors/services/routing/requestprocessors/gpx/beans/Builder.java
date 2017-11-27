package heigit.ors.services.routing.requestprocessors.gpx.beans;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class Builder extends GPX {


    // Not implemented types --> Have not been needed so far.
    // Element track = new Element("trk");
    // Element trackExtensions = new Element("extensions");
    public Builder() {


        // https://stackoverflow.com/questions/7373567/how-to-read-and-write-xml-files
        // Hiermit die einzelnen Elemente bauen

    }

    public String build(GPX gpx) throws JAXBException {
        // TODO finish
        Element gpxRoot = new Element("gpx");
        Document xml = new Document();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        // TODO add creator String that leads to some specifications for the output on the ors site
        String creatorString = "ORS " + year + " - https://www.openrouteservice.org\"";
        Namespace version = Namespace.getNamespace("version", "1.1");
        Namespace creator = Namespace.getNamespace("creator", creatorString);
        gpxRoot.addNamespaceDeclaration(version);
        gpxRoot.addNamespaceDeclaration(creator);
        gpxRoot.addContent(metadataBuilder(gpx));

        // check for each gpx structure
        if (!gpx.get_points().isEmpty()) {
            List<WayPoint> rawPoints = gpx.get_points();
            for (WayPoint pointElement : rawPoints) {
                gpxRoot.addContent(wptBuilder(pointElement));
            }
        }
        if (!gpx.get_routes().isEmpty()) {
            List<Route> rawRoutes = gpx.get_routes();
            for (Route routeElement : rawRoutes) {
                gpxRoot.addContent(routeBuilder(routeElement));
            }

        }
        if (!gpx.get_tracks().isEmpty()) {
            List<Track> rawTracks = gpx.get_tracks();
            for (Track trackElement : rawTracks) {
                gpxRoot.addContent(trackBuilder(trackElement));
            }
        }
        xml.setRootElement(gpxRoot);

        return xml.toString();


    }


    private Element metadataBuilder(GPX gpx) {
        Element metadataXml = new Element("metadata");
        Element metadataXmlExtensions = new Element("extensions");
        // TODO decide what information needs to be added as metadata
        return metadataXml;
    }

    private Element routeBuilder(Route route) {
        Element rteXml = new Element("rte");
        Element rteXmlExtensions = new Element("extensions");
        // TODO iterate over the waypoints and add them as children to rteXml with rtePointBuilder
        return rteXml;
    }

    private Element rtePointBuilder() {
        Element rtePointXml = new Element("rtept");
        // actual extensions of the waypoints!!!!
        Element rtePointXmlExtensions = new Element("extensions");
        // TODO generate the pure rtept xml here. No need of wptBuilder
        // x and y go as namespace and the rest as children!
        return rtePointXml;
    }

    private Element trackBuilder(Track track) {
        Element trkXml = new Element("trk");
        Element trkPointXmlExtensions = new Element("extensions");
        return trkXml;
    }

    private Element trkPointBuilder() {
        Element trkPointXml = new Element("trkpt");
        Element trkPointXmlExtension = new Element("extensions");
        // x and y go as namespace and the rest as children!
        return trkPointXml;
    }

    // wptBuilder isnt in use yet. GPX will only provide routes for now
    private Element wptBuilder(WayPoint wayPoint) {
        Element wayPointXml = new Element("wpt");
        Element wayPointXmlExtensions = new Element("extensions");


        return wayPointXml;
    }

}
