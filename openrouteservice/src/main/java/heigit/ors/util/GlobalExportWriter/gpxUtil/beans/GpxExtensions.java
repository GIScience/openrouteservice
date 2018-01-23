package heigit.ors.util.GlobalExportWriter.gpxUtil.beans;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link GpxExtensions} represents the extensions for {@link Gpx}
 * Can be manually extended
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "attribution",
        "engine",
        "build_date",
        "profile",
        "preference",
        "language",
        "distance_units",
        "duration_units",
        "instructions",
        "elevation"

        // always add new variables here! and below
})

public class GpxExtensions extends ExtensionsType{

    protected String attribution;
    protected String engine;
    protected String build_date;
    protected String profile;
    protected String preference;
    protected String language;
    @XmlElement(name = "distance-units")
    protected String distance_units;
    @XmlElement(name = "duration-units")
    protected String duration_units;
    protected String instructions;
    protected String elevation;

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getBuild_date() {
        return build_date;
    }

    public void setBuild_date(String build_date) {
        this.build_date = build_date;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDistance_units() {
        return distance_units;
    }

    public void setDistance_units(String distance_units) {
        this.distance_units = distance_units;
    }

    public String getDuration_units() {
        return duration_units;
    }

    public void setDuration_units(String duration_units) {
        this.duration_units = duration_units;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }
}



