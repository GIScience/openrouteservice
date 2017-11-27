package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.util.Calendar;

public class Copyright {
    private String _author;
    private String _year;
    private String _licence;


    Copyright() {
        // TODO add _licence
        _licence = "_licence to be added";
        int curr_year = Calendar.getInstance().get(Calendar.YEAR);
        _author = "OpenStreetMap contributors";
        _year = String.valueOf(curr_year);
    }

    public String get_author() {
        return _author;
    }

    public String get_year() {
        return _year;
    }

    public String get_licence() {
        return _licence;
    }


}
