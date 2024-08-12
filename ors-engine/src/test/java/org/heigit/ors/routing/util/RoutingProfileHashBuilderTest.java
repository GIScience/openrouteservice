package org.heigit.ors.routing.util;

import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RoutingProfileHashBuilderTest {

    static final String EMPTY = "";
    static final String BLANK = " ";

    @Test
    void createHash() {
    }

    @Test
    public void withBoolean(){
        assertEquals("true", RoutingProfileHashBuilder.builder().withBoolean(true).getConcatenatedValues());
        assertEquals("false", RoutingProfileHashBuilder.builder().withBoolean(false).getConcatenatedValues());
    }
    @Test
    public void withBooleanObj(){
        assertEquals("null", RoutingProfileHashBuilder.builder().withBoolean(null).getConcatenatedValues());
        assertEquals("true", RoutingProfileHashBuilder.builder().withBoolean(Boolean.TRUE).getConcatenatedValues());
        assertEquals("false", RoutingProfileHashBuilder.builder().withBoolean(Boolean.FALSE).getConcatenatedValues());
    }

    @Test
    public void withDouble(){
        assertEquals("1.000000", RoutingProfileHashBuilder.builder().withDouble(1d).getConcatenatedValues());
        assertEquals("1.123457", RoutingProfileHashBuilder.builder().withDouble(1.123456789012d).getConcatenatedValues());
        assertEquals("0.000000", RoutingProfileHashBuilder.builder().withDouble(0d).getConcatenatedValues());
        assertEquals("0.000000", RoutingProfileHashBuilder.builder().withDouble(0d).getConcatenatedValues());
    }

    @Test
    public void withDoubleObj(){
        assertEquals("1.000000", RoutingProfileHashBuilder.builder().withDouble(Double.valueOf(1d)).getConcatenatedValues());
        assertEquals("1.123457", RoutingProfileHashBuilder.builder().withDouble(Double.valueOf(1.123456789012d)).getConcatenatedValues());
        assertEquals("0.000000", RoutingProfileHashBuilder.builder().withDouble(Double.valueOf(0d)).getConcatenatedValues());
        assertEquals("null", RoutingProfileHashBuilder.builder().withDouble(null).getConcatenatedValues());
    }

    @Test
    public void withInteger(){
        assertEquals("1", RoutingProfileHashBuilder.builder().withInteger(1).getConcatenatedValues());
        assertEquals("0", RoutingProfileHashBuilder.builder().withInteger(0).getConcatenatedValues());
        assertEquals("-1", RoutingProfileHashBuilder.builder().withInteger(-1).getConcatenatedValues());
    }

    @Test
    public void withIntegerObj(){
        assertEquals("1", RoutingProfileHashBuilder.builder().withInteger(Integer.valueOf(1)).getConcatenatedValues());
        assertEquals("0", RoutingProfileHashBuilder.builder().withInteger(Integer.valueOf(0)).getConcatenatedValues());
        assertEquals("-1", RoutingProfileHashBuilder.builder().withInteger(Integer.valueOf(-1)).getConcatenatedValues());
        assertEquals("null", RoutingProfileHashBuilder.builder().withInteger(null).getConcatenatedValues());
    }

    @Test
    public void withString(){
        assertEquals("EMPTY", RoutingProfileHashBuilder.builder().withString("").getConcatenatedValues());
        assertEquals("aString", RoutingProfileHashBuilder.builder().withString(String.valueOf("aString")).getConcatenatedValues());
        assertEquals(" ", RoutingProfileHashBuilder.builder().withString(String.valueOf(" ")).getConcatenatedValues());
        assertEquals("null", RoutingProfileHashBuilder.builder().withString(null).getConcatenatedValues());
    }

    @Test
    public void withNamedString(){
        assertEquals("aName<EMPTY>", RoutingProfileHashBuilder.builder().withNamedString("aName", "").getConcatenatedValues());
        assertEquals("aName<aString>", RoutingProfileHashBuilder.builder().withNamedString("aName", String.valueOf("aString")).getConcatenatedValues());
        assertEquals("aName< >", RoutingProfileHashBuilder.builder().withNamedString("aName", String.valueOf(" ")).getConcatenatedValues());
        assertEquals("aName<null>", RoutingProfileHashBuilder.builder().withNamedString("aName", null).getConcatenatedValues());

        assertThrows(IllegalArgumentException.class, () -> RoutingProfileHashBuilder.builder().withNamedString(BLANK, String.valueOf("aString")).getConcatenatedValues());
        assertThrows(IllegalArgumentException.class, () -> RoutingProfileHashBuilder.builder().withNamedString(EMPTY, String.valueOf("aString")).getConcatenatedValues());
        assertThrows(IllegalArgumentException.class, () -> RoutingProfileHashBuilder.builder().withNamedString(null, String.valueOf("aString")).getConcatenatedValues());
    }

    @Test
    public void withMap(){
        assertEquals("testmap()", RoutingProfileHashBuilder.builder().withMapStringString(null, "testmap").getConcatenatedValues());

        Map<String,String> map = new HashMap<>();
        assertEquals("testmap()", RoutingProfileHashBuilder.builder().withMapStringString(map, "testmap").getConcatenatedValues());

        map.put("value", "value");
        map.put("null", null);
        map.put("empty", EMPTY);
        map.put("blank", BLANK);
        assertEquals("testmap(blank= ,empty=EMPTY,null=null,value=value)", RoutingProfileHashBuilder.builder().withMapStringString(map, "testmap").getConcatenatedValues());
    }
    @Test
    public void withMapOfMaps(){
        assertEquals("mapOfMaps()", RoutingProfileHashBuilder.builder().withMapOfMaps(null, "mapOfMaps").getConcatenatedValues());

        Map<String,Map<String,String>> mapOfMaps = new HashMap<>();
        assertEquals("mapOfMaps()", RoutingProfileHashBuilder.builder().withMapOfMaps(mapOfMaps, "mapOfMaps").getConcatenatedValues());

        Map<String, String> filledMap = new HashMap<>();
        filledMap.put("value", "value");
        filledMap.put("empty", "");
        filledMap.put("blank", " ");
        filledMap.put("null", null);
        mapOfMaps.put("filledMap", filledMap);
        mapOfMaps.put("nullMap", null);
        mapOfMaps.put("emptyMap", new HashMap<>());
        assertEquals("mapOfMaps(emptyMap(),filledMap(blank= ,empty=EMPTY,null=null,value=value),nullMap())", RoutingProfileHashBuilder.builder().withMapOfMaps(mapOfMaps, "mapOfMaps").getConcatenatedValues());
    }

    static class CountingHashCollector {
        public int callCount = 0;
        public Set<String> hashes = new HashSet<>();
        void createHashAndCount(ORSGraphHopperConfig ghConfig){
            callCount++;
            String profileHash = RoutingProfileHashBuilder.builder("0", ghConfig).build();
            hashes.add(profileHash);
            System.out.println(profileHash);
        }
    }

    private static ORSGraphHopperConfig createORSGraphHopperConfig() {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfigWithoutOsmFile();
        ghConfig.putObject("datareader.file", "src/test/files/preprocessed_osm_data.pbf");
        return ghConfig;
    }

    private static ORSGraphHopperConfig createORSGraphHopperConfigWithoutOsmFile() {
        ORSGraphHopperConfig ghConfig = new ORSGraphHopperConfig();
        ghConfig.putObject("graph.dataaccess", "RAM");
        ghConfig.putObject("graph.location", "unittest.testgraph");
        ghConfig.setProfiles(List.of(new Profile("blah").setVehicle("car").setWeighting("fastest").setTurnCosts(true)));
        return ghConfig;
    }

    @Test
    public void createProfileHashWithORSGraphHopperConfig() {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfig();
        CountingHashCollector collector = new CountingHashCollector();
        collector.createHashAndCount(ghConfig);

        ghConfig.getProfiles().get(0).setName("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getProfiles().get(0).setVehicle("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getProfiles().get(0).setTurnCosts(!ghConfig.getProfiles().get(0).isTurnCosts());
        collector.createHashAndCount(ghConfig);

        ghConfig.getProfiles().get(0).setWeighting("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getCHProfiles().add(new CHProfile("added"));
        collector.createHashAndCount(ghConfig);

        ghConfig.getLMProfiles().add(new LMProfile("added"));
        collector.createHashAndCount(ghConfig);

        ghConfig.getLMProfiles().get(0).setPreparationProfile("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getLMProfiles().get(0).setPreparationProfile("this");
        ghConfig.getLMProfiles().get(0).setMaximumLMWeight(ghConfig.getLMProfiles().get(0).getMaximumLMWeight()-1);
        collector.createHashAndCount(ghConfig);

        PMap pMap = ghConfig.asPMap();
        assertNotNull(pMap);

        ghConfig.getFastisochroneProfiles().add(new Profile("added"));
        collector.createHashAndCount(ghConfig);

        ghConfig.getFastisochroneProfiles().get(0).setVehicle("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getFastisochroneProfiles().get(0).setTurnCosts(!ghConfig.getProfiles().get(0).isTurnCosts());
        collector.createHashAndCount(ghConfig);

        ghConfig.getFastisochroneProfiles().get(0).setWeighting("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getCoreProfiles().add(new CHProfile("added"));
        collector.createHashAndCount(ghConfig);

        ghConfig.getCoreLMProfiles().add(new LMProfile("added"));
        collector.createHashAndCount(ghConfig);

        ghConfig.getCoreLMProfiles().get(0).setPreparationProfile("changed");
        collector.createHashAndCount(ghConfig);

        ghConfig.getCoreLMProfiles().get(0).setPreparationProfile("this");
        ghConfig.getCoreLMProfiles().get(0).setMaximumLMWeight(ghConfig.getLMProfiles().get(0).getMaximumLMWeight()-1);
        collector.createHashAndCount(ghConfig);

        assertEquals(collector.hashes.size(), collector.callCount);
    }
}