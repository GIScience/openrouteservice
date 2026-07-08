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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvGraphStorageBuilderTest {

    private CsvGraphStorageBuilder createBuilder(Path csvPath) {
        CsvGraphStorageBuilder builder = new CsvGraphStorageBuilder();
        ExtendedStorageProperties parameters = new ExtendedStorageProperties();
        parameters.setFilepath(csvPath);
        builder.setParameters(parameters);
        return builder;
    }

    /**
     * Test that init throws GraphStorageException when the CSV file does not exist.
     */
    @Test
    void testInitMissingCsvFile() {
        CsvGraphStorageBuilder builder = createBuilder(Path.of("nonexistent_file_that_does_not_exist.csv"));

        GraphStorageException ex = assertThrows(GraphStorageException.class, () -> builder.init(new GraphHopper()));
        assertTrue(ex.getMessage().contains("could not read CSV file"), "Expected 'could not read CSV file' in: " + ex.getMessage());
    }

    /**
     * Test that init throws GraphStorageException when the CSV file contains non-numeric values.
     */
    @Test
    void testInitInvalidCsvValues() {
        Path invalidCsv = Path.of("src/test/files/csv/invalid_values.csv");
        CsvGraphStorageBuilder builder = createBuilder(invalidCsv);

        GraphStorageException ex = assertThrows(GraphStorageException.class, () -> builder.init(new GraphHopper()));
        assertTrue(ex.getMessage().contains("could not parse CSV file"), "Expected 'could not parse CSV file' in: " + ex.getMessage());
    }

    /**
     * Test that init succeeds with a well-formed CSV file.
     */
    @Test
    void testInitValidCsvFile() {
        Path validCsv = Path.of("src/test/files/csv/valid.csv");
        CsvGraphStorageBuilder builder = createBuilder(validCsv);

        assertDoesNotThrow(() -> builder.init(new GraphHopper()));
    }
}
