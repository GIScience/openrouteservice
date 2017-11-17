package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.time.ZonedDateTime;

public class Metadata {
    private final String _name;
    private final String _description;
    private final String _author;
    private final String _copyright;
    private final Bounds _bounds;

    public Metadata(String name, String description, String author, String copyright, Bounds bounds) {
        _name = name;
        _description = description;
        _author = author;
        _copyright = copyright;
        _bounds = bounds;
    }
}
