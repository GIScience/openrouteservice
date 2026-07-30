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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.reader.ReaderNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PbfElevationProviderTest {
    private PbfElevationProvider elevationProvider;

    @BeforeEach
    void setUp() {
        elevationProvider = new PbfElevationProvider();
    }

    @AfterEach
    void tearDown() {
        elevationProvider.release();
    }

    @Test
    void testGetEleForNodeWithValidElevation() {
        ReaderNode node = new ReaderNode(1, 0.0, 0.0, Map.of("ele", "100"));
        assertEquals(100.0,  elevationProvider.getEle(node), "Elevation should match the node's elevation value.");
    }

    @Test
    void testGetEleForNodeWithInvalidElevation() {
        ReaderNode node = new ReaderNode(1, 0.0, 0.0, Map.of("ele", "abc"));
        assertEquals(0.0,  elevationProvider.getEle(node), "Invalid elevation should default to 0.");
    }

    @Test
    void testGetEleForNodeWithMissingElevation() {
        ReaderNode node = new ReaderNode(1, 0.0, 0.0);
        assertEquals(0.0,  elevationProvider.getEle(node), "Missing elevation should default to 0.");
    }

    @Test
    void testGetEleForNodeLogsWarningOnce() {
        ReaderNode node = new ReaderNode(1, 0.0, 0.0);

        assertFalse(elevationProvider.isErrorLogged());
        elevationProvider.getEle(node);
        assertTrue(elevationProvider.isErrorLogged());
    }

    @Test
    void testGetEleForCoordinatesThrowsException() {
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> elevationProvider.getEle(0.0, 0.0),
                "Expected UnsupportedOperationException for getEle with coordinates."
        );
        assertEquals("PbfElevationProvider does not support resolving elevation from coordinates.", exception.getMessage());
    }

    @Test
    void testCanInterpolate() {
        assertFalse(elevationProvider.canInterpolate());
    }
}
