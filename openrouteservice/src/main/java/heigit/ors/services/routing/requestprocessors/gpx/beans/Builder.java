package heigit.ors.services.routing.requestprocessors.gpx.beans;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

public class Builder {
    public Builder() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String creator = "";
        String version = "1.1";
        String header = "";
        String footer = "";
        // https://stackoverflow.com/questions/7373567/how-to-read-and-write-xml-files
        // Hiermit die einzelnen Elemente bauen

    }

    public Builder write(GPX gpx) throws JAXBException {
        if (!gpx.get_routes().isEmpty()){
            XMLStreamWriter
        }
        // TODO write function
        return null;
    }
}
