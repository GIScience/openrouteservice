package heigit.ors.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class GeomUtilityTest {
    @Test
    public void TestMetresToDegrees() {
        assertEquals(1, GeomUtility.metresToDegrees(111139), 0.0);
    }

    @Test
    public void TestDegreesToMetres() {
        assertEquals(111139, GeomUtility.degreesToMetres(1), 0.0);
    }
}
