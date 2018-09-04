package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.config.AppConfig;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class GPXLink {
    @XmlAttribute(name = "href")
    private String href;
    @XmlElement(name = "text")
    private String text;
    @XmlElement
    private String type;

    public GPXLink() {
        this.href = AppConfig.Global().getParameter("info", "base_url");
        this.text = AppConfig.Global().getParameter("info", "base_url");
        this.type = "text/html";
    }

}
