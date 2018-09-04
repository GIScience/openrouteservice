package heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import heigit.ors.util.FormatUtility;

import java.io.IOException;

public class BBoxSerializer extends StdSerializer<double[][]> {
    private final int COORDINATE_PRECISION = 6;

    public BBoxSerializer() {
        this(null);
    }

    public BBoxSerializer(Class<double[][]> t) {
        super(t);
    }

    @Override
    public void serialize(double[][] coordinates, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        double[][] formattedCoords = new double[coordinates.length][coordinates[0].length];
        formattedCoords[0][0] = FormatUtility.roundToDecimals(coordinates[0][0], COORDINATE_PRECISION);
        formattedCoords[0][1] = FormatUtility.roundToDecimals(coordinates[0][1], COORDINATE_PRECISION);
        formattedCoords[1][0] = FormatUtility.roundToDecimals(coordinates[1][0], COORDINATE_PRECISION);
        formattedCoords[1][1] = FormatUtility.roundToDecimals(coordinates[1][1], COORDINATE_PRECISION);

        if(coordinates[0].length == 3)
            formattedCoords[0][2] = FormatUtility.roundToDecimals(coordinates[0][2], COORDINATE_PRECISION);
        if(coordinates[1].length == 3)
            formattedCoords[1][2] = FormatUtility.roundToDecimals(coordinates[0][2], COORDINATE_PRECISION);

        jsonGenerator.writeStartArray();
        jsonGenerator.writeArray(formattedCoords[0], 0, formattedCoords[0].length);
        jsonGenerator.writeArray(formattedCoords[1], 0, formattedCoords[1].length);
        jsonGenerator.writeEndArray();
    }
}
