package org.heigit.ors.isochrones.statistics.postgresql;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgresSQLStatisticsProviderTest {

    @Test
    void initToleratesUnsetPasswordAndPort() {
        Map<String, Object> params = new HashMap<>();
        params.put("host", "127.0.0.1");
        params.put("port", null);
        params.put("user", "ors");
        params.put("password", null);
        params.put("db_name", "ors");
        params.put("table_name", "pop");
        params.put("geometry_column", "rast");
        params.put("postgis_version", null);

        PostgresSQLStatisticsProvider provider = new PostgresSQLStatisticsProvider();
        // No database is reachable, so the pool fails to start but at connect time and not with an NPE.
        Exception e = assertThrows(Exception.class, () -> provider.init(params));
        assertFalse(e instanceof NullPointerException, "init() failed with " + e);
    }
}
