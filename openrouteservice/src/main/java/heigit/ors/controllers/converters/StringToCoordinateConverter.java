package heigit.ors.controllers.converters;

import com.vividsolutions.jts.geom.Coordinate;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;

public class StringToCoordinateConverter implements Converter<String, Coordinate> {
    @Override
    public Coordinate convert(String from) {
        String[] data = from.split(",");
        Coordinate c = new Coordinate();
        c.x = Double.parseDouble(data[0]);
        c.y = Double.parseDouble(data[1]);

        return c;
    }

}
