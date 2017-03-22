package heigit.ors.util;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;

public class OrderedJSONObjectFactory {
    private static Logger log = Logger.getLogger(OrderedJSONObjectFactory.class.getName());
    private static boolean setupDone = false;
    private static Field JSONObjectMapField = null;

    private static void setupFieldAccessor() {
        if( !setupDone ) {
            setupDone = true;
            try {
                JSONObjectMapField = JSONObject.class.getDeclaredField("map");
                JSONObjectMapField.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
                log.warning("JSONObject implementation has changed, returning unmodified instance");
            }
        }
    }

    public static JSONObject create() {
        setupFieldAccessor();
        JSONObject result = new JSONObject();
        try {
            if (JSONObjectMapField != null) {
                JSONObjectMapField.set(result, new LinkedHashMap<>());
            }
        }catch (IllegalAccessException ignored) {}
        return result;
    }
}