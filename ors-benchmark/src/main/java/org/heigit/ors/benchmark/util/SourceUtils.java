package org.heigit.ors.benchmark.util;

import org.heigit.ors.benchmark.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.csv;

public class SourceUtils {
    private static final Logger logger = LoggerFactory.getLogger(SourceUtils.class);
    private static final String PROFILE_COLUMN = "profile";

    public static List<Map<String, Object>> getRecordsByProfile(String sourceFile, TestConfig config) throws IllegalStateException {
        // Read all records from CSV
        List<Map<String, Object>> records = csv(sourceFile).readRecords();
        logger.info("Read {} records from CSV file", records.size());

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
            logger.info("No profile column found in CSV, using all {} coordinates", records.size());
        } else {
            // Group records by profile
            recordsByProfile = records.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            coordinateRecord -> (String) coordinateRecord.getOrDefault(PROFILE_COLUMN,
                                    config.getTargetProfile())));
            logger.info("Found coordinates for profiles: {}", recordsByProfile.keySet());
        }

        List<Map<String, Object>> targetRecords = recordsByProfile.getOrDefault(config.getTargetProfile(), recordsByProfile.get("all"));

        if (targetRecords == null || targetRecords.isEmpty()) {
            throw new IllegalStateException("No records found for profile '" + config.getTargetProfile() + "' in file " + sourceFile);
        }

        logger.info("Selected {} records for profile '{}'", targetRecords.size(), config.getTargetProfile());
        return targetRecords;
    }

    public static Iterator<Map<String, Object>> getRecordFeeder(List<Map<String, Object>> targetRecords, TestConfig config) {
        // Transform records to coordinates and shuffle
        List<Map<String, Object>> mappedRecords = targetRecords.stream()
                .map(targetRecord -> Map.of(
                        config.getFieldLon(), targetRecord.get(config.getFieldLon()),
                        config.getFieldLat(), targetRecord.get(config.getFieldLat())))
                .collect(Collectors.toList());
        Collections.shuffle(mappedRecords);

        Iterator<Map<String, Object>> recordFeeder = IteratorUtils.infiniteCircularIterator(mappedRecords);
        logger.info("Created circular feeder with {} coordinates for profile {}", mappedRecords.size(), config.getTargetProfile());

        return recordFeeder;
    }
}
