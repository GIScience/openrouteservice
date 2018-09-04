package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.config.AppConfig;
import heigit.ors.services.routing.RoutingServiceSettings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class GPXCopyright {
    @XmlAttribute(name = "author")
    private String author;
    @XmlElement(name = "year")
    private int year;
    @XmlElement(name = "license")
    private String license;

    public GPXCopyright() {
        this.author = RoutingServiceSettings.getAttribution();
        this.license = AppConfig.Global().getParameter("info", "content_licence");
        this.year = Calendar.getInstance().get(Calendar.YEAR);
    }
}
