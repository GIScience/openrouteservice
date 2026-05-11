package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.Border;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.config.profile.ExtendedStorageName;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import org.heigit.ors.util.ErrorLoggingUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;


public class BorderParser implements TagParser {
    private final EnumEncodedValue<Border> borderEnc;

    private static final String PARAM_KEY_IDS = "ids";
    private static final String PARAM_KEY_BOUNDARIES = "boundaries";
    private static final String PARAM_KEY_OPEN_BORDERS = "openborders";
    private static final String TAG_KEY_COUNTRY = "country";
    private static final String TAG_KEY_COUNTRY1 = "country1";
    private static final String TAG_KEY_COUNTRY2 = "country2";
    private final ExtendedStorageProperties parameters;
    private CountryBordersReader cbReader;
    boolean preprocessed = false;
    private Map<Integer, Map<String, String>> nodeTags;


    public BorderParser(ORSGraphHopper orsGraphHopper) {
        this(new EnumEncodedValue<>(Border.KEY, Border.class), orsGraphHopper);
    }

    public BorderParser(EnumEncodedValue<Border> borderEnc, ORSGraphHopper orsGraphHopper) {
        this.borderEnc = borderEnc;
        this.parameters = orsGraphHopper.getProfileProperties().getBuild().getExtStorages().get(ExtendedStorageName.BORDERS.getName());

        try {
            init(orsGraphHopper);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing BorderParser", e);
        }

    }
    public void init(GraphHopper graphhopper) throws Exception {
        File expectedStorageFileLocation1 = Path.of(graphhopper.getGraphHopperLocation() + "/ext_borders").toFile();
        File expectedStorageFileLocation2 = Path.of(graphhopper.getGraphHopperLocation() + "/ext_borders_cbr").toFile();

        if (this.cbReader == null && (!expectedStorageFileLocation1.exists() || !expectedStorageFileLocation2.exists())) {
            cbReader = createCountryBordersReader();
            cbReader.serialize(expectedStorageFileLocation2);
        }
        if (cbReader == null && expectedStorageFileLocation2.exists()) {
            cbReader = CountryBordersReader.deserialize(expectedStorageFileLocation2);
        }
    }

    private CountryBordersReader createCountryBordersReader() throws IOException {
        String bordersFile = "";
        String countryIdsFile = "";
        String openBordersFile = "";

        preprocessed = Boolean.TRUE.equals(parameters.getPreprocessed());

        if (!preprocessed) {
            if (parameters.getBoundaries() != null) {
                bordersFile = parameters.getBoundaries().toString();
            } else {
                ErrorLoggingUtility.logMissingConfigParameter(BordersGraphStorageBuilder.class, PARAM_KEY_BOUNDARIES);
                // We cannot continue without the information
                throw new MissingResourceException("An OSM file enriched with country tags or a boundary geometry file is needed to use the borders extended storage!", BordersGraphStorage.class.getName(), PARAM_KEY_BOUNDARIES);
            }
        }

        if (parameters.getIds() != null)
            countryIdsFile = parameters.getIds().toString();
        else
            ErrorLoggingUtility.logMissingConfigParameter(BordersGraphStorageBuilder.class, PARAM_KEY_IDS);

        if (parameters.getOpenborders() != null)
            openBordersFile = parameters.getOpenborders().toString();
        else
            ErrorLoggingUtility.logMissingConfigParameter(BordersGraphStorageBuilder.class, PARAM_KEY_OPEN_BORDERS);

        // Read the file containing all the country border polygons
        return new CountryBordersReader(bordersFile, countryIdsFile, openBordersFile);
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(borderEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        return edgeFlags;
    }

    @Override
    public IntsRef handleWayTags(int fromIndex, int toIndex, IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        if (cbReader == null) {
            return edgeFlags;
        }

        short countryId1 = 0;
        short countryId2 = 0;
        if (preprocessed) {
            nodeTags = way.getTag("ors:node_tags", new HashMap<>());
            countryId1 = getCountryIdForNode(fromIndex);
            countryId2 = getCountryIdForNode(toIndex);
        } else {
            countryId1 = getCountryIdFromWay(way, TAG_KEY_COUNTRY1);
            countryId2 = getCountryIdFromWay(way, TAG_KEY_COUNTRY2);
        }

        short countryId = countryId1 == 0 ? countryId2 : countryId1;

        if (countryId != 0)
            way.setTag("ors:country", cbReader.getCountry(countryId));
        borderEnc.setEnum(false, edgeFlags, getBorderType(countryId1, countryId2));

        return edgeFlags;
    }

    private int idToTowerNode(int id) {
        return -id - 3;
    }

    private short getCountryIdForNode(int nodeId) {
        int towerNode = idToTowerNode(nodeId);
        String countryCode = nodeTags.getOrDefault(towerNode, new HashMap<>()).getOrDefault(TAG_KEY_COUNTRY, "");
        try {
            return CountryBordersReader.getCountryIdByISOCode(countryCode);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private short getCountryIdFromWay(ReaderWay way, String tagKey) {
        String countryValue = way.getTag(tagKey);
        try {
            return Short.parseShort(cbReader.getId(countryValue));
        } catch (Exception ignore) {
            return 0;
        }
    }

    private Border getBorderType(short countryId1, short countryId2) {
        if (countryId1 == countryId2) {
            return Border.NONE;
        }
        String countryName1 = cbReader.getName(countryId1);
        String countryName2 = cbReader.getName(countryId2);

        return cbReader.isOpen(countryName1, countryName2) ? Border.OPEN : Border.CONTROLLED;
    }
}
