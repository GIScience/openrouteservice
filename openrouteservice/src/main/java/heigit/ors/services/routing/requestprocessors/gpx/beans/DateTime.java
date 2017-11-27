package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateTime {
    private DateFormat dateFormat;
    private String currDate;

    DateTime() {
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        currDate = dateFormat.format(date);
    }
    public String getTime(){
        return currDate;
    }
}
