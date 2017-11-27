package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.util.Calendar;

public class Person {
    private String name;
    private String email;
    private String link;

    Person() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        name = "ORS " + year + " - https://www.openrouteservice.org\"";
        email = "support@openrouteservice.org";
        link = "https://www.openrouteservice.org/";
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLink() {
        return link;
    }
}
