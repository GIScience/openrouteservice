package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.AccessRestriction;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessRestrictionParserTest {
    private IntEncodedValue accessRestrictionEnc;
    private AccessRestrictionsParser parser;
    private IntsRef intsRef, relFlags;


    void initParser(int profileType) {
        parser = new AccessRestrictionsParser(profileType);
        EncodingManager em = new EncodingManager.Builder().add(parser).build();
        accessRestrictionEnc = em.getIntEncodedValue(AccessRestriction.KEY);
        relFlags = em.createRelationFlags();
        intsRef = em.createEdgeFlags();
    }

    private void assertRestrictionsForProfile(int profileType) {
        initParser(profileType);
        ReaderWay way = new ReaderWay(1);

        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NONE, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "no");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NO, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "customers");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.CUSTOMERS, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "delivery");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DELIVERY, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "private");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.PRIVATE, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "permissive");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.PERMISSIVE, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("access", "permit");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.PERMIT, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testAccessTypesCar() {
        assertRestrictionsForProfile(RoutingProfileType.DRIVING_CAR);
    }

    @Test
    void testAccessCycling() {
        assertRestrictionsForProfile(RoutingProfileType.CYCLING_REGULAR);
    }

    @Test
    void testCarWayCreation() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NO, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("motorcar", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("motorcar", "yes");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NONE, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testVehicleDestination() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("vehicle", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testHgvDestination() {
        initParser(RoutingProfileType.DRIVING_HGV);

        ReaderWay way = new ReaderWay(1);

        way.setTag("hgv", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testAccessNoVehicleDestination() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("access", "no");
        way.setTag("vehicle", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testMotorcarDestinationMotorvehicleYes() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("motorcar", "destination");
        way.setTag("motor_vehicle", "yes");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testMotorcarYesVehicleDestination() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("motorcar", "yes");
        way.setTag("vehicle", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NONE, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testMotorcarDestinationAccessNo() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("motorcar", "destination");
        way.setTag("access", "no");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testAccessPrivateCustomers() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("access", "private;customers");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.PRIVATE | AccessRestrictionType.CUSTOMERS, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testMotorcarPrivateCustomers() {
        initParser(RoutingProfileType.DRIVING_CAR);

        ReaderWay way = new ReaderWay(1);

        way.setTag("motorcar", "private;customers");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.PRIVATE | AccessRestrictionType.CUSTOMERS, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testBikeWayCreation() {
        initParser(RoutingProfileType.CYCLING_REGULAR);

        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NO, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("bicycle", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("bicycle", "yes");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NONE, accessRestrictionEnc.getInt(false, intsRef));
    }

    @Test
    void testFootWayCreation() {
        initParser(RoutingProfileType.FOOT_WALKING);

        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NO, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("foot", "destination");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.DESTINATION, accessRestrictionEnc.getInt(false, intsRef));

        way.setTag("foot", "yes");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(AccessRestrictionType.NONE, accessRestrictionEnc.getInt(false, intsRef));
    }
}