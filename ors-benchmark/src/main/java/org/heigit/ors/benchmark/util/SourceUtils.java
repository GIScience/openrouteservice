package org.heigit.ors.benchmark.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.heigit.ors.benchmark.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.gatling.javaapi.core.CoreDsl.csv;

public class SourceUtils {
    private SourceUtils() {
        // Private constructor to hide implicit public one
    }

    private static final Logger logger = LoggerFactory.getLogger(SourceUtils.class);
    private static final String PROFILE_COLUMN = "profile";

    public static List<Map<String, Object>> getRecordsByProfile(String sourceFile, String targetProfile)
            throws IllegalStateException {
        // Read all records from CSV
        List<Map<String, Object>> records = csv(sourceFile).readRecords();
        logger.debug("Read {} records from CSV file", records.size());

        if (records.isEmpty()) {
            throw new IllegalStateException("No records found in CSV file: " + sourceFile);
        }

        // Sample log of first record for debugging
        logger.debug("Sample record structure: {}", records.get(0).keySet());

        // Group records by profile if profile column exists, otherwise use all records
        Map<String, List<Map<String, Object>>> recordsByProfile;
        if (records.isEmpty() || !records.get(2).containsKey(PROFILE_COLUMN)) {
            // If no profile column exists, put all records under a default key
            recordsByProfile = Map.of("all", records);
            logger.debug("No profile column found in CSV, using all {} coordinates", records.size());
        } else {
            // Group records by profile
            recordsByProfile = records.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            coordinateRecord -> (String) coordinateRecord.getOrDefault(PROFILE_COLUMN,
                                    targetProfile)));
            logger.debug("Found coordinates for profiles: {}", recordsByProfile.keySet());
        }

        List<Map<String, Object>> targetRecords = recordsByProfile.getOrDefault(targetProfile,
                recordsByProfile.get("all"));

        if (targetRecords == null || targetRecords.isEmpty()) {
            throw new IllegalStateException(
                    "No records found for profile '" + targetProfile + "' in file " + sourceFile);
        }

        logger.debug("Selected {} records for profile '{}'", targetRecords.size(), targetProfile);
        return targetRecords;
    }

    public static Iterator<Map<String, Object>> getRecordFeeder(List<Map<String, Object>> targetRecords,
            TestConfig config, String targetProfile) {
        // Transform records to coordinates and shuffle
        List<Map<String, Object>> mappedRecords = !targetRecords.isEmpty()
                && targetRecords.get(0).containsKey(config.getFieldLon())
                && targetRecords.get(0).containsKey(config.getFieldLat())
                        ? targetRecords.stream()
                                .map(targetRecord -> Map.of(
                                        config.getFieldLon(), targetRecord.get(config.getFieldLon()),
                                        config.getFieldLat(), targetRecord.get(config.getFieldLat())))
                                .toList()
                        : targetRecords.stream()
                                .map(targetRecord -> Map.of(
                                        config.getFieldStartLon(), targetRecord.get(config.getFieldStartLon()),
                                        config.getFieldStartLat(), targetRecord.get(config.getFieldStartLat()),
                                        config.getFieldEndLon(), targetRecord.get(config.getFieldEndLon()),
                                        config.getFieldEndLat(), targetRecord.get(config.getFieldEndLat())))
                                .toList();
        try {
            // Shuffle the records
            logger.debug("Shuffling records for profile {}", targetProfile);
            List<Map<String, Object>> mutableRecords = new ArrayList<>(mappedRecords);
            Collections.shuffle(mutableRecords);
            mappedRecords = mutableRecords;
            logger.debug("Shuffled {} records for profile {}", mappedRecords.size(), targetProfile);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("Failed to shuffle records", e);
        }

        Iterator<Map<String, Object>> recordFeeder = IteratorUtils.infiniteCircularIterator(mappedRecords);
        logger.debug("Created circular feeder with {} coordinates for profile {}", mappedRecords.size(), targetProfile);

        return recordFeeder;
    }
}
