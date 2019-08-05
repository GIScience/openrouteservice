/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package heigit.ors.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import heigit.ors.exceptions.ParameterValueException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Iterator;

public class JsonUtility {

    public static int[] parseIntArray(JSONArray array, String elemName, int errorCode) throws Exception {
        if (array.length() <= 0)
            return null;

        try {
            int[] res = new int[array.length()];
            for (int i = 0; i < array.length(); i++)
                res[i] = array.getInt(i);
            return res;
        } catch (Exception ex) {
            throw new ParameterValueException(errorCode, "Unable to parse the element '" + elemName + "'. " + ex.getMessage());
        }
    }

    public static JSONObject objectToJSONObject(Object object) {
        Object json = null;
        JSONObject jsonObject = null;
        try {
            json = new JSONTokener(object.toString()).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json instanceof JSONObject) {
            jsonObject = (JSONObject) json;
        }
        return jsonObject;
    }

    public static JSONArray objectToJSONArray(Object object) {
        Object json = null;
        JSONArray jsonArray = null;
        try {
            json = new JSONTokener(object.toString()).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        }
        return jsonArray;
    }

    public static JsonNode convertJsonFormat(JSONObject json) {
        ObjectNode ret = JsonNodeFactory.instance.objectNode();

        @SuppressWarnings("unchecked")
        Iterator<String> iterator = json.keys();
        for (; iterator.hasNext();) {
            String key = iterator.next();
            Object value;
            try {
                value = json.get(key);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (json.isNull(key))
                ret.putNull(key);
            else if (value instanceof String)
                ret.put(key, (String) value);
            else if (value instanceof Integer)
                ret.put(key, (Integer) value);
            else if (value instanceof Long)
                ret.put(key, (Long) value);
            else if (value instanceof Double)
                ret.put(key, (Double) value);
            else if (value instanceof Boolean)
                ret.put(key, (Boolean) value);
            else if (value instanceof JSONObject)
                ret.put(key, convertJsonFormat((JSONObject) value));
            else if (value instanceof JSONArray)
                ret.put(key, convertJsonFormat((JSONArray) value));
            else
                throw new RuntimeException("not prepared for converting instance of class " + value.getClass());
        }
        return ret;
    }

    public static JsonNode convertJsonFormat(JSONArray json) {
        ArrayNode ret = JsonNodeFactory.instance.arrayNode();
        for (int i = 0; i < json.length(); i++) {
            Object value;
            try {
                value = json.get(i);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (json.isNull(i))
                ret.addNull();
            else if (value instanceof String)
                ret.add((String) value);
            else if (value instanceof Integer)
                ret.add((Integer) value);
            else if (value instanceof Long)
                ret.add((Long) value);
            else if (value instanceof Double)
                ret.add((Double) value);
            else if (value instanceof Boolean)
                ret.add((Boolean) value);
            else if (value instanceof JSONObject)
                ret.add(convertJsonFormat((JSONObject) value));
            else if (value instanceof JSONArray)
                ret.add(convertJsonFormat((JSONArray) value));
            else
                throw new RuntimeException("not prepared for converting instance of class " + value.getClass());
        }
        return ret;
    }
}
