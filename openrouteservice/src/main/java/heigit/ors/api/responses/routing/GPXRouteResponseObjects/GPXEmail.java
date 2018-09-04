package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.config.AppConfig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "email")
public class GPXEmail {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "domain")
    private String domain;

    public GPXEmail() {
        String email = AppConfig.Global().getParameter("info", "support_mail");
        String[] parts = email.split("@");

        if(parts.length == 2) {
            id = parts[0];
            domain = parts[1];
        }
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }
}