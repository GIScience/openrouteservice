package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Storable;

import java.time.Instant;

/**
 * Simple storage designed to hold edgeID - direction - speed
 * Speeds should be in kph
 * Indexed by edgeIds
 *
 * @author Hendrik Leuschner
 */
public class CommonSpeedStorage extends SpeedStorage {
    private static final int BYTE_POS_TIMESTAMP = 1;
    private static final int BYTE_POS_TIMESTAMP_REVERSE = 6;
    private static final int EXPIRATION_TIME = 15; //in minutes
    private final FlagEncoder flagEncoder;

    public CommonSpeedStorage(int edgeCount, FlagEncoder flagEncoder, Directory dir) {
        super(edgeCount);
        this.flagEncoder = flagEncoder;
    }

    /**
     * Create a time stamp from unix time in minutes
     * @return current unix time in minutes
     */
    private int createIntTimeStamp(){
        return Math.toIntExact(Instant.now().getEpochSecond() / 60);
    }

}
