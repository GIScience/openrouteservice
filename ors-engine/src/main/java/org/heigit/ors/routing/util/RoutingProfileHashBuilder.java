package org.heigit.ors.routing.util;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperConfig;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

//TODO adapt to new configuration if required
// (or ideally just use profileProperties.graphBuildingProperties.hashCode()?)
public class RoutingProfileHashBuilder {

    private RoutingProfileHashBuilder(){}

    private final List<String> propertyValues = new ArrayList<>();

    public static RoutingProfileHashBuilder builder(){
        return new RoutingProfileHashBuilder();
    }

    public static RoutingProfileHashBuilder builder(String graphVersion, ProfileProperties profileProperties) {
        RoutingProfileHashBuilder hashBuilder = RoutingProfileHashBuilder.builder()
                .withString(graphVersion)
//                .withString(profileProperties.getName())
                .withBoolean(profileProperties.getEnabled())
//                .withString(profileProperties.getProfiles())
//                .withString(profileProperties.getGraphPath())
//                .withMapOfMaps(profileProperties.getExtStorages(), "extStorages")
//                .withMapOfMaps(profileProperties.getGraphBuilders(), "graphBuilders")
                .withDouble(profileProperties.getMaximumDistance())
                .withDouble(profileProperties.getMaximumDistanceDynamicWeights())
                .withDouble(profileProperties.getMaximumDistanceAvoidAreas())
                .withDouble(profileProperties.getMaximumDistanceAlternativeRoutes())
                .withDouble(profileProperties.getMaximumDistanceRoundTripRoutes())
                .withDouble(profileProperties.getMaximumWayPoints())
                .withBoolean(profileProperties.getInstructions())
                .withBoolean(profileProperties.getOptimize())
                .withInteger(profileProperties.getEncoderFlagsSize())
//                .withString(profileProperties.getEncoderOptions())
//                .withString(profileProperties.getGtfsFile())
//                .withObject(profileProperties.getIsochronePreparationOpts())
//                .withObject(profileProperties.getPreparationOpts())
//                .withObject(profileProperties.getExecutionOpts())
//                .withString(profileProperties.getElevationProvider())
//                .withString(profileProperties.getElevationCachePath())
//                .withString(profileProperties.getElevationDataAccess())
//                .withBoolean(profileProperties.getElevationCacheClear())
                .withBoolean(profileProperties.getElevationSmoothing())
                .withBoolean(profileProperties.getInterpolateBridgesAndTunnels())
                .withInteger(profileProperties.getMaximumSnappingRadius())
//                .withObject(profileProperties.getExtent())
//                .withBoolean(profileProperties.hasMaximumSnappingRadius())
                .withInteger(profileProperties.getLocationIndexResolution())
                .withInteger(profileProperties.getLocationIndexSearchIterations())
                .withDouble(profileProperties.getMaximumSpeedLowerBound())
//                .withInteger(profileProperties.getTrafficExpirationMin())
//                .withInteger(profileProperties.getMaximumVisitedNodesPT())
//                .withBoolean(profileProperties.isTurnCostEnabled())
//                .withBoolean(profileProperties.isEnforceTurnCosts())
                ;

        return hashBuilder;
    }

    public static RoutingProfileHashBuilder builder(String graphVersion, GraphHopperConfig config) {
        // File name or hash should not be contained in the hash, same hast should map to different graphs built off different files for the same region/profile pair.
        Map<String, Object> configWithoutFilePath = config.asPMap().toMap();
        configWithoutFilePath.remove("datareader.file");
        configWithoutFilePath.remove("graph.dataaccess");
        configWithoutFilePath.remove("graph.location");
        configWithoutFilePath.remove("graph.elevation.provider");
        configWithoutFilePath.remove("graph.elevation.cache_dir");
        configWithoutFilePath.remove("graph.elevation.dataaccess");
        configWithoutFilePath.remove("graph.elevation.clear");
        RoutingProfileHashBuilder builder = RoutingProfileHashBuilder.builder()
                .withString(graphVersion)
                .withNamedString("profiles", config.getProfiles().stream().map(Profile::toString).sorted().collect(Collectors.joining()))
                .withNamedString("chProfiles", config.getCHProfiles().stream().map(CHProfile::toString).sorted().collect(Collectors.joining()))
                .withNamedString("lmProfiles", config.getLMProfiles().stream().map(LMProfile::toString).sorted().collect(Collectors.joining()))
                .withMapStringObject(configWithoutFilePath, "pMap");
        if (config instanceof ORSGraphHopperConfig orsConfig) {
            builder.withNamedString("coreProfiles", orsConfig.getCoreProfiles().stream().map(CHProfile::toString).sorted().collect(Collectors.joining()))
                    .withNamedString("coreLMProfiles", orsConfig.getCoreLMProfiles().stream().map(LMProfile::toString).sorted().collect(Collectors.joining()))
                    .withNamedString("fastisochroneProfiles", orsConfig.getFastisochroneProfiles().stream().map(Profile::toString).sorted().collect(Collectors.joining()));
        }
        return builder;
    }

    public String build(){
        String joinedString = getConcatenatedValues();
        byte[] bytes = joinedString.getBytes();
        return DigestUtils.md5DigestAsHex(bytes);
    }

    public String getConcatenatedValues() {
        return String.join(",", propertyValues);
    }

    public RoutingProfileHashBuilder withNamedString(String name, String value) {
        if (StringUtils.isBlank(name)){
            throw new IllegalArgumentException("first argument 'name' must not be blank");
        }
        propertyValues.add("%s<%s>".formatted(name, valueOfString(value)));
        return this;
    }
    public RoutingProfileHashBuilder withString(String value) {
        String notnullValue = valueOfString(value);
        propertyValues.add(notnullValue);
        return this;
    }

    private String valueOfString(String value) {
        String notNullValue = String.valueOf(value);
        return StringUtils.isEmpty(notNullValue) ? "EMPTY" : notNullValue;
    }

    public RoutingProfileHashBuilder withDouble(Double value) {
        propertyValues.add(String.format("%f", value));
        return this;
    }

    public RoutingProfileHashBuilder withDouble(double value) {
        propertyValues.add(String.format("%f", value));
        return this;
    }

    public RoutingProfileHashBuilder withInteger(int value) {
        propertyValues.add(String.format("%d", value));
        return this;
    }

    public RoutingProfileHashBuilder withObject(Object object) {
        return withString(ofNullable(object).map(Object::toString).orElse(null));
    }

    public RoutingProfileHashBuilder withInteger(Integer value) {
        propertyValues.add(String.format("%d", value));
        return this;
    }

    public RoutingProfileHashBuilder withBoolean(Boolean value) {
        propertyValues.add(String.format("%s", value));
        return this;
    }

    public RoutingProfileHashBuilder withMapStringString(Map<String,String> map, String name){
        withString(valueOfMapStringString(map, name));
        return this;
    }

    private String valueOfMapStringString(Map<String, String> map, String name) {
        if (map == null) {
            return String.format("%s()", name);
        }

        String content = map.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getKey().compareTo(
                        i2.getKey()))
                .map( e -> e.getKey()+"="+valueOfString(e.getValue()))
                .collect(Collectors.joining(","));

        return String.format("%s(%s)", name, content);
    }

    public RoutingProfileHashBuilder withMapStringObject(Map<String,Object> map, String name){
        withString(valueOfMapStringObject(map, name));
        return this;
    }

    private String valueOfMapStringObject(Map<String, Object> map, String name) {
        if (map == null) {
            return String.format("%s()", name);
        }

        String content = map.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getKey().compareTo(
                        i2.getKey()))
                .map( e -> e.getKey()+"="+valueOfString(ofNullable(e.getValue()).map(Objects::toString).orElse(null)))
                .collect(Collectors.joining(","));

        return String.format("%s(%s)", name, content);
    }

    public RoutingProfileHashBuilder withMapOfMaps(Map<String, Map<String,String>> mapOfMaps, String name){
        if (mapOfMaps == null) {
            withString(String.format("%s()", name));
            return this;
        }

        String content = mapOfMaps.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getKey().compareTo(
                        i2.getKey()))
                .map( e -> valueOfMapStringString(e.getValue(), e.getKey()))
                .collect(Collectors.joining(","));

        withString(String.format("%s(%s)", name, content));
        return this;
    }

}
