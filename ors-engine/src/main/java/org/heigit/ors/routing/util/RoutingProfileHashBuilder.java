package org.heigit.ors.routing.util;

import org.apache.commons.lang3.StringUtils;
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
    public String build(){
        String joinedString = getConcatenatedValues();
        byte[] bytes = joinedString.getBytes();
        return DigestUtils.md5DigestAsHex(bytes);
    }

    public String getConcatenatedValues() {
        return String.join(",", propertyValues);
    }

    public RoutingProfileHashBuilder withNamedString(String name, String value) {
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
