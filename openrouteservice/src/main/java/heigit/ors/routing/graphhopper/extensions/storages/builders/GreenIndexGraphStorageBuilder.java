package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.GreenIndexGraphStorage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lliu on 13/03/2017.
 */
public class GreenIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private GreenIndexGraphStorage _storage;
    private Map<Long, Double> _greenIndices = new HashMap<>();
    private static int TOTAL_LEVEL = 64;
    private Map<Byte, SlotRange> _slots = new HashMap<>(TOTAL_LEVEL);

    public GreenIndexGraphStorageBuilder() {

    }

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Check if the _greenIndexFile exists
        String csvFile = _parameters.get("filepath");
        readGreenIndicesFromCSV(csvFile);
        prepareGreenIndexSlots();
        _storage = new GreenIndexGraphStorage();

        return _storage;
    }

    private void prepareGreenIndexSlots() {
        double max = Collections.max(_greenIndices.values());
        double min = Collections.min(_greenIndices.values());
        double step = (max - min) / TOTAL_LEVEL;
        // Divide the range of raw green index values into TOTAL_LEVEL,
        // then map the raw value to [0..TOTAL_LEVEL - 1]
        for (byte i = 0; i < TOTAL_LEVEL; i++) {
            _slots.put(i, new SlotRange(min + i * step, min + (i + 1) * step));
        }
    }

    private void readGreenIndicesFromCSV(String csvFile) throws IOException {
        BufferedReader csvBuffer = null;
        try {
            String row;
            csvBuffer = new BufferedReader(new FileReader(csvFile));
            // Jump the header line
            csvBuffer.readLine();
            while ((row = csvBuffer.readLine()) != null) {
                ArrayList<String> parsedRow = parseCSVrow(row);
                if (parsedRow == null) continue;
                _greenIndices.put(Long.parseLong(parsedRow.get(0)), Double.parseDouble(parsedRow.get(1)));
            }

        } catch (IOException openFileEx) {
            openFileEx.printStackTrace();
            throw openFileEx;
        } finally {
            if (csvBuffer != null) csvBuffer.close();
        }
    }

    private ArrayList<String> parseCSVrow(String row) {
        ArrayList<String> result = new ArrayList<>();
        if (row != null) {
            String[] splitData = row.split("\\s*,\\s*");
            for (String col : splitData) {
                if ((col != null) && (col.length() > 0)) {
                    result.add(col.trim());
                } else {
                    return null;
                }
            }
        }
        return result;
    }

    @Override
    public void processWay(OSMWay way) {

    }

    @Override
    public void processEdge(OSMWay way, EdgeIteratorState edge) {
        _storage.setEdgeValue(edge.getEdge(), calcGreenIndex(way.getId()));
    }

    private class SlotRange {
        double left = 0.0;
        double right = 0.0;

        SlotRange(double l, double r) {
            this.left = l;
            this.right = r;
        }

        boolean within(double val) {
            // check if the @val falls in (left, right] range
            if ((val <= left) || (val > right)) return false;
            return true;
        }
    }

    private byte calcGreenIndex(long id) {
        Double gi = _greenIndices.get(id);
        for (Map.Entry<Byte, SlotRange> s : _slots.entrySet()) {
            if (gi != null) {
                if (s.getValue().within(_greenIndices.get(id)))
                    return s.getKey();
            } else {
                // No such @id key in the _greenIndices, or the value of it is null
                // We set its green level to TOTAL_LEVEL/2 indicating the middle value for such cases
                return (byte) (TOTAL_LEVEL / 2);
            }
        }
        return (byte) (TOTAL_LEVEL - 1);
    }

    @Override
    public String getName() {
        return "GreenIndex";
    }
}
