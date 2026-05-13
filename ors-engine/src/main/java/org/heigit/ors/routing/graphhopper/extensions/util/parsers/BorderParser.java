package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.config.profile.ExtendedStorageName;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
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
    private final EnumEncodedValue<Country> countryEnc;

    private static final String PARAM_KEY_IDS = "ids";
    private static final String PARAM_KEY_BOUNDARIES = "boundaries";
    private static final String PARAM_KEY_OPEN_BORDERS = "openborders";
    private static final String TAG_KEY_COUNTRY = "country";
    private static final String TAG_KEY_COUNTRY1 = "country1";
    private static final String TAG_KEY_COUNTRY2 = "country2";
    private final ExtendedStorageProperties parameters;

    public CountryBordersReader getCbReader() {
        return cbReader;
    }

    private CountryBordersReader cbReader;
    boolean preprocessed = false;
    private Map<Integer, Map<String, String>> nodeTags;


    public BorderParser(ORSGraphHopper orsGraphHopper) {
        this.borderEnc = Border.create();
        this.countryEnc = CountryOther.create();
        this.parameters = orsGraphHopper.getProfileProperties().getBuild().getExtStorages().get(ExtendedStorageName.BORDERS.getName());

        try {
            init(orsGraphHopper);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing BorderParser", e);
        }

    }
    public void init(ORSGraphHopper orsGraphHopper) throws Exception {
        File expectedStorageFileLocation = Path.of(orsGraphHopper.getGraphHopperLocation() + "/country_borders_reader").toFile();

        if (cbReader == null && !expectedStorageFileLocation.exists()) {
            cbReader = createCountryBordersReader();
            cbReader.serialize(expectedStorageFileLocation);
        }
        if (cbReader == null && expectedStorageFileLocation.exists()) {
            cbReader = CountryBordersReader.deserialize(expectedStorageFileLocation);
        }

        orsGraphHopper.getProcessContext().setCountryBordersReader(cbReader);
    }

    private CountryBordersReader createCountryBordersReader() throws IOException {
        String bordersFile = "";
        String countryIdsFile = "";
        String openBordersFile = "";

        preprocessed = Boolean.TRUE.equals(parameters.getPreprocessed());

        //FIXME: read borders file regardless of the preprocessed flag, as it is used to verify countries for border edges in extra info processor
        if (!preprocessed) {
            if (parameters.getBoundaries() != null) {
                bordersFile = parameters.getBoundaries().toString();
            } else {
                ErrorLoggingUtility.logMissingConfigParameter(BorderParser.class, PARAM_KEY_BOUNDARIES);
                // We cannot continue without the information
                throw new MissingResourceException("An OSM file enriched with country tags or a boundary geometry file is needed to use the borders extended storage!", BorderParser.class.getName(), PARAM_KEY_BOUNDARIES);
            }
        }

        if (parameters.getIds() != null)
            countryIdsFile = parameters.getIds().toString();
        else
            ErrorLoggingUtility.logMissingConfigParameter(BorderParser.class, PARAM_KEY_IDS);

        if (parameters.getOpenborders() != null)
            openBordersFile = parameters.getOpenborders().toString();
        else
            ErrorLoggingUtility.logMissingConfigParameter(BorderParser.class, PARAM_KEY_OPEN_BORDERS);

        // Read the file containing all the country border polygons
        return new CountryBordersReader(bordersFile, countryIdsFile, openBordersFile);
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(borderEnc);
        list.add(countryEnc);
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

        if (countryId1 != 0) {
            way.setTag("ors:country", cbReader.getCountry(countryId1));
        }
        if (countryId2 != 0) {
            countryEnc.setEnum(false, edgeFlags, cbReader.getCountry(countryId2));
        }

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
