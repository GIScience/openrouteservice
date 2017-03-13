package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.GreenIndexGraphStorage;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lliu on 13/03/2017.
 */
public class GreenIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private GreenIndexGraphStorage _storage;
    private String _greenIndexFile;
    private Map<Long, Double> _greenIndices = new HashMap<>();

    public GreenIndexGraphStorageBuilder() {

    }

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        _greenIndexFile = _parameters.get("filepath");
        // TODO Check if the _greenIndexFile exists
        String csvFile = _greenIndexFile;
        _greenIndices = readGreenIndicesFromCSV(csvFile);
        _storage = new GreenIndexGraphStorage();

        return _storage;
    }

    private Map<Long, Double> readGreenIndicesFromCSV(String csvFile) throws IOException {
        Map<Long, Double> csvData = new HashMap<>();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            String[] line;
            // Jump the header line
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                csvData.put(Long.parseLong(line[0]), Double.parseDouble(line[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return csvData;
    }

    @Override
    public void processWay(OSMWay way) {

    }

    @Override
    public void processEdge(OSMWay way, EdgeIteratorState edge) {

    }

    @Override
    public String getName() {
        return "GreenIndex";
    }
}
