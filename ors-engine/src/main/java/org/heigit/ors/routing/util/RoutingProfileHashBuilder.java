package org.heigit.ors.routing.util;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperConfig;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class RoutingProfileHashBuilder {

    private RoutingProfileHashBuilder(){}

    private final List<String> propertyValues = new ArrayList<>();

    public static RoutingProfileHashBuilder builder(){
        return new RoutingProfileHashBuilder();
    }

    public static RoutingProfileHashBuilder builder(RouteProfileConfiguration routeProfileConfiguration) {
        RoutingProfileHashBuilder hashBuilder = RoutingProfileHashBuilder.builder()
                .withString(routeProfileConfiguration.getName())
                .withBoolean(routeProfileConfiguration.getEnabled())
                .withString(routeProfileConfiguration.getProfiles())
//                .withString(routeProfileConfiguration.getGraphPath())
                .withMapOfMaps(routeProfileConfiguration.getExtStorages(), "extStorages")
                .withMapOfMaps(routeProfileConfiguration.getGraphBuilders(), "graphBuilders")
                .withDouble(routeProfileConfiguration.getMaximumDistance())
                .withDouble(routeProfileConfiguration.getMaximumDistanceDynamicWeights())
                .withDouble(routeProfileConfiguration.getMaximumDistanceAvoidAreas())
                .withDouble(routeProfileConfiguration.getMaximumDistanceAlternativeRoutes())
                .withDouble(routeProfileConfiguration.getMaximumDistanceRoundTripRoutes())
                .withDouble(routeProfileConfiguration.getMaximumWayPoints())
                .withBoolean(routeProfileConfiguration.getInstructions())
                .withBoolean(routeProfileConfiguration.getOptimize())
                .withInteger(routeProfileConfiguration.getEncoderFlagsSize())
                .withString(routeProfileConfiguration.getEncoderOptions())
                .withString(routeProfileConfiguration.getGtfsFile())
                .withObject(routeProfileConfiguration.getIsochronePreparationOpts())
                .withObject(routeProfileConfiguration.getPreparationOpts())
                .withObject(routeProfileConfiguration.getExecutionOpts())
                .withString(routeProfileConfiguration.getElevationProvider())
                .withString(routeProfileConfiguration.getElevationCachePath())
                .withString(routeProfileConfiguration.getElevationDataAccess())
                .withBoolean(routeProfileConfiguration.getElevationCacheClear())
                .withBoolean(routeProfileConfiguration.getElevationSmoothing())
                .withBoolean(routeProfileConfiguration.getInterpolateBridgesAndTunnels())
                .withInteger(routeProfileConfiguration.getMaximumSnappingRadius())
//                .withObject(routeProfileConfiguration.getExtent())
                .withBoolean(routeProfileConfiguration.hasMaximumSnappingRadius())
                .withInteger(routeProfileConfiguration.getLocationIndexResolution())
                .withInteger(routeProfileConfiguration.getLocationIndexSearchIterations())
                .withDouble(routeProfileConfiguration.getMaximumSpeedLowerBound())
//                .withInteger(routeProfileConfiguration.getTrafficExpirationMin())
                .withInteger(routeProfileConfiguration.getMaximumVisitedNodesPT())
                .withBoolean(routeProfileConfiguration.isTurnCostEnabled())
                .withBoolean(routeProfileConfiguration.isEnforceTurnCosts());

        return hashBuilder;
    }

    public static RoutingProfileHashBuilder builder(GraphHopperConfig config) {
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
