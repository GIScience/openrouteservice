package heigit.ors.controllersOld.converters2;

import com.vividsolutions.jts.geom.Coordinate;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public class StringToCoordinateSequenceWrapperConverter implements Converter<String, CoordinateSequenceWrapper> {
    @Override
    public CoordinateSequenceWrapper convert(String coordinatesString) {
        List<Coordinate> coordinates = new ArrayList<>();

        String[] parts = coordinatesString.split(",");

        if(parts.length % 2 != 0)
            throw new IllegalArgumentException("Coordinates must be in pairs");

        for(int i=0; i<=parts.length-2; i+=2) {
            Coordinate c = new Coordinate();
            try {
                c.x = Double.parseDouble(parts[i]);
                c.y = Double.parseDouble(parts[i + 1]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid coordinate value: " + nfe.getMessage());
            }
            coordinates.add(c);
        }

        return new CoordinateSequenceWrapper(coordinates);
    }
}
