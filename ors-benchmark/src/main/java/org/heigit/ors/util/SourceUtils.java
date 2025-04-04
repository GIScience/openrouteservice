package org.heigit.ors.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SourceUtils {
    private SourceUtils() {
        // Private constructor to hide implicit public one
    }

    private static final Logger logger = LoggerFactory.getLogger(SourceUtils.class);
    private static final String PROFILE_COLUMN = "profile";

    public static List<Map<String, Object>> getRecordsByProfile(List<Map<String, Object>> records, String targetProfile)
            throws IllegalStateException {
        // Read all records from CSV
        logger.debug("Read {} records from CSV file", records.size());

        if (records.isEmpty()) {
            throw new IllegalStateException("No records found in CSV file: " + records);
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
                    "No records found for profile '" + targetProfile + "' in file " + records);
        }

        logger.debug("Selected {} records for profile '{}'", targetRecords.size(), targetProfile);
        try {
            // Shuffle the records
            logger.debug("Shuffling records for profile {}", targetProfile);
            List<Map<String, Object>> mutableRecords = new ArrayList<>(targetRecords);
            Collections.shuffle(mutableRecords);
            targetRecords = mutableRecords;
            logger.debug("Shuffled {} records for profile {}", targetRecords.size(), targetProfile);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("Failed to shuffle records", e);
        }
        return targetRecords;
    }
}
