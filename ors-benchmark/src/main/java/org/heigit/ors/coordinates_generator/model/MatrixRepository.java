package org.heigit.ors.coordinates_generator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MatrixRepository {
    private final Map<String, List<Matrix>> matricesByProfile;
    private final Map<String, Set<Matrix>> uniqueMatricesByProfile;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected static final Logger LOGGER = LoggerFactory.getLogger(MatrixRepository.class);

    public MatrixRepository(Set<String> profiles) {
        matricesByProfile = new HashMap<>();
        uniqueMatricesByProfile = new HashMap<>();
        
        for (String profile : profiles) {
            matricesByProfile.put(profile, new ArrayList<>());
            uniqueMatricesByProfile.put(profile, new HashSet<>());
        }
    }
    
    public boolean addMatrix(Matrix matrix) {
        String profile = matrix.getProfile();
        
        lock.writeLock().lock();
        try {
            if (uniqueMatricesByProfile.get(profile).add(matrix)) {
                matricesByProfile.get(profile).add(matrix);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("Error adding matrix: {}", e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean addMatrixIfUnique(Matrix matrix) {
        return addMatrix(matrix);
    }
    
    public void clear() {
        lock.writeLock().lock();
        try {
            matricesByProfile.values().forEach(List::clear);
            uniqueMatricesByProfile.values().forEach(Set::clear);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int getMatrixCount(String profile) {
        lock.readLock().lock();
        try {
            return uniqueMatricesByProfile.get(profile).size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public int getTotalMatrixCount() {
        lock.readLock().lock();
        try {
            return uniqueMatricesByProfile.values().stream()
                    .mapToInt(Set::size)
                    .sum();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean isProfileComplete(String profile, int requiredMatrices) {
        lock.readLock().lock();
        try {
            return uniqueMatricesByProfile.get(profile).size() >= requiredMatrices;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean areAllProfilesComplete(int requiredMatrices) {
        lock.readLock().lock();
        try {
            return uniqueMatricesByProfile.values().stream()
                    .allMatch(matrices -> matrices.size() >= requiredMatrices);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<Matrix> getAllMatrices(int limit) {
        lock.readLock().lock();
        try {
            List<Matrix> allMatrices = new ArrayList<>();
            matricesByProfile.values().forEach(matrices ->
                    matrices.stream().limit(limit).forEach(allMatrices::add));
            return allMatrices;
        } finally {
            lock.readLock().unlock();
        }
    }
    /**
     * Writes all matrix objects grouped by profile to a CSV file.
     * Each matrix is serialized as a single row, with all array fields
     * converted to string representations. This method includes all
     * matrices in the dataset.
     *
     * The CSV file will have the following header:
     * coordinates,sources,destinations,distances,profile
     *
     * Example of a generated row:
     * "[[8.667542, 49.440851], [8.668100, 49.440900], ....]","[0, 1, 2, 3]","[4, 5]","[[10.5, 20.7, ...], [15.2, 25.4, ...], ...]", "driving-car"
     *
     * Each field is enclosed in quotes to safely include commas inside arrays.
     * - coordinates: 2D array of latitude and longitude pairs
     * - sources: 1D array of source indices
     * - destinations: 1D array of destination indices
     * - distances: 2D array of distances (meters), one sub-array per source
     * - profile: routing profile (e.g., "driving", "walking")
     *
     * @param filePath the file path to write the CSV output to
     * @throws FileNotFoundException if the file cannot be created or opened
     */
    public void writeToCSV(String filePath) throws FileNotFoundException {
        lock.readLock().lock();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            // CSV header
            pw.println("coordinates,sources,destinations,distances,profile");

            matricesByProfile.forEach((profile, matrices) ->
                    matrices.forEach(matrix -> {
                        String coordinatesStr = Arrays.deepToString(matrix.getCoordinates());
                        String sourcesStr = Arrays.toString(matrix.getSources());
                        String destinationsStr = Arrays.toString(matrix.getDestinations());
                        String distancesStr = Arrays.deepToString(matrix.getDistances());
                        String profileStr = matrix.getProfile();

                        // Write CSV row with quoted strings to handle commas
                        pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                                coordinatesStr,
                                sourcesStr,
                                destinationsStr,
                                distancesStr,
                                profileStr
                        );
                    })
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Writes all matrix objects grouped by profile to a CSV file.
     * Each matrix is serialized as a single row, with all array fields
     * converted to string representations. This method limits max number of written elements.
     *
     * The CSV file will have the following header:
     * coordinates,sources,destinations,distances,profile
     *
     * Example of a generated row:
     * "[[8.667542, 49.440851], [8.668100, 49.440900], ....]","[0, 1, 2, 3]","[4, 5]","[[10.5, 20.7, ...], [15.2, 25.4, ...], ...]", "driving-car"
     *
     * Each field is enclosed in quotes to safely include commas inside arrays.
     * - coordinates: 2D array of latitude and longitude pairs
     * - sources: 1D array of source indices
     * - destinations: 1D array of destination indices
     * - distances: 2D array of distances (meters), one sub-array per source
     * - profile: routing profile (e.g., "driving", "walking")
     *
     * @param filePath the file path to write the CSV output to
     * @throws FileNotFoundException if the file cannot be created or opened
     */
    public void writeToCSV(String filePath, int limit) throws FileNotFoundException {
        lock.readLock().lock();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            // CSV header
            pw.println("coordinates,sources,destinations,distances,profile");

            matricesByProfile.forEach((profile, matrices) ->
                    matrices.stream().limit(limit).forEach(matrix -> {
                        String coordinatesStr = Arrays.deepToString(matrix.getCoordinates());
                        String sourcesStr = Arrays.toString(matrix.getSources());
                        String destinationsStr = Arrays.toString(matrix.getDestinations());
                        String distancesStr = Arrays.deepToString(matrix.getDistances());
                        String profileStr = matrix.getProfile();

                        pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                                coordinatesStr,
                                sourcesStr,
                                destinationsStr,
                                distancesStr,
                                profileStr
                        );
                    })
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public String getProgressMessage() {
        lock.readLock().lock();
        try {
            return uniqueMatricesByProfile.entrySet().stream()
                .map(e -> String.format("%s: %d", e.getKey(), e.getValue().size()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        } finally {
            lock.readLock().unlock();
        }
    }
}
