package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.util.Helper;

import java.util.HashMap;
import java.util.Map;

public class ORSPMap extends HintsMap {
    private final Map<String, Object> objMap = new HashMap<>();

    public void putObj(String key, Object obj) {
        objMap.put(Helper.camelCaseToUnderScore(key), obj);
    }

    public boolean hasObj(String key) {
        return objMap.containsKey(Helper.camelCaseToUnderScore(key));
    }

    public Object getObj(String key) {
        if (Helper.isEmpty(key)) {
            return "";
        }
        return objMap.get(Helper.camelCaseToUnderScore(key));
    }
}
