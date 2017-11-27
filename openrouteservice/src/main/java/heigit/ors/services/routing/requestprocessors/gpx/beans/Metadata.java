package heigit.ors.services.routing.requestprocessors.gpx.beans;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Metadata {
    private String _name;
    private String _description;
    private Person author;
    private Copyright copyright;
    //private link = new Link(); --> Not needed so far. TODO Could be used to store the original url??!!
    private String _dateTime;
    private Bounds bounds;
    private Extensions extensions;

    public Metadata() {
        author = new Person();
        copyright = new Copyright();
        _dateTime = new DateTime().getTime();
    }


    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public Person getAuthor() {
        return author;
    }

    public Copyright getCopyright() {
        return copyright;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public String get_dateTime() {
        return _dateTime;
    }

    public void setExtensions() {
        this.extensions = null;
    }

    public Object getExtension(String key) {
        return extensions.getValue(key);
    }

    public void addExtension(String key, Object value) {
        extensions.addValue(key, value);
    }
}
