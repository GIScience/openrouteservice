package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import org.heigit.ors.kafka.ORSKafkaConsumerMessageSpeedUpdate;

import java.time.Instant;

/**
 * Stores speeds and an accompanying time stamp that can be used to expire the validity after a set time.
 */
public class ExpiringSpeedStorage extends SpeedStorage {
    private static final long BYTE_COUNT = 10; //One byte for forward speed, one byte for backward speed, 2 x 4 bytes for time stamp in unix time minutes
    private static final long BYTE_POS_SPEED = 0;
    private static final long BYTE_POS_SPEED_REVERSE = 5;
    private static final long BYTE_POS_TIMESTAMP = 1;
    private static final long BYTE_POS_TIMESTAMP_REVERSE = 6;
    private int defaultExpirationTime = 15; //in minutes

    public ExpiringSpeedStorage(FlagEncoder flagEncoder) {
        super(flagEncoder);
    }

    @Override
    public void init(Graph graph, Directory directory) {
        this.speedData = directory.find("ext_expiring_speeds_" + this.flagEncoder.toString());
    }

    /**
     * Creates the storage and defaults all values to Byte.MIN_VALUE
     *
     * @param edgeCount
     * @return The storage
     */
    @Override
    public SpeedStorage create(long edgeCount) {
        this.edgeCount = (int) edgeCount;
        speedData.create(BYTE_COUNT * edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            this.setSpeed(i, false, Byte.MIN_VALUE);
            this.setSpeed(i, true, Byte.MIN_VALUE);
        }
        return this;
    }

    public void process(ORSKafkaConsumerMessageSpeedUpdate msg) {
        if(!isValid(msg))
            throw new IllegalArgumentException("Invalid kafka message");
        byte speed = (byte) msg.getSpeed();
        this.setSpeed(msg.getEdgeId(), msg.isReverse(), speed, msg.getDurationMin());
    }

    @Override
    public void setSpeed(int edgeId, boolean reverse, byte speed) {
        checkEdgeInBounds(edgeId);
        speedData.setBytes(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_SPEED_REVERSE : BYTE_POS_SPEED), new byte[]{speed}, 1);
        speedData.setInt(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_TIMESTAMP_REVERSE : BYTE_POS_TIMESTAMP), createIntTimeStamp() + defaultExpirationTime);
    }

    public void setSpeed(int edgeId, boolean reverse, byte speed, int expirationTimeMin) {
        checkEdgeInBounds(edgeId);
        speedData.setBytes(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_SPEED_REVERSE : BYTE_POS_SPEED), new byte[]{speed}, 1);
        speedData.setInt(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_TIMESTAMP_REVERSE : BYTE_POS_TIMESTAMP), createIntTimeStamp() + expirationTimeMin);
    }

    /**
     * get the speed. Return only if not expired. Return Byte.MIN_VALUE if no speed set.
     * @param edgeId
     * @param reverse
     * @return the speed
     */
    @Override
    public int getSpeed(int edgeId, boolean reverse) {
        checkEdgeInBounds(edgeId);
        byte[] speedByte = new byte[1];
        speedData.getBytes(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_SPEED_REVERSE : BYTE_POS_SPEED), speedByte, 1);
        if (speedByte[0] == Byte.MIN_VALUE)
            return Byte.MIN_VALUE;

        int storedTimeStamp = speedData.getInt(BYTE_COUNT * edgeId + (reverse ? ExpiringSpeedStorage.BYTE_POS_TIMESTAMP_REVERSE : ExpiringSpeedStorage.BYTE_POS_TIMESTAMP));
        if (createIntTimeStamp() > storedTimeStamp)
            return Byte.MIN_VALUE;

        return speedByte[0];
    }

    /**
     * Create a time stamp from unix time in minutes
     *
     * @return current unix time in minutes
     */
    private int createIntTimeStamp() {
        return Math.toIntExact(Instant.now().getEpochSecond() / 60);
    }

    /**
     * Set the expiration time in minutes
     * @param newDefaultExpirationTimeMinutes
     */
    public void setDefaultExpirationTime(int newDefaultExpirationTimeMinutes) {
        this.defaultExpirationTime = newDefaultExpirationTimeMinutes;
    }

    private boolean isValid(ORSKafkaConsumerMessageSpeedUpdate msg) {
        return !(msg.getSpeed() > Byte.MAX_VALUE || msg.getSpeed() < 0);
    }

}
