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

    //TODO Adapt to matrix
    public void writeToCSV(String filePath) throws FileNotFoundException {
        lock.readLock().lock();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance,profile");
            
            matricesByProfile.forEach((profile, matrices) ->
                    matrices.forEach(matrix -> pw.printf(Locale.US, "%f,%f,%f,%f,%.2f,%s%n",
                    route.getStart()[0], route.getStart()[1],
                    route.getEnd()[0], route.getEnd()[1],
                    route.getDistance(),
                    route.getProfile())
                )
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    //TODO Adapt to matrix
    public void writeToCSV(String filePath, int limit) throws FileNotFoundException {
        lock.readLock().lock();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance,profile");
            
            matricesByProfile.forEach((profile, routes) ->
                routes.stream().limit(limit).forEach(route -> pw.printf(Locale.US, "%f,%f,%f,%f,%.2f,%s%n",
                    route.getStart()[0], route.getStart()[1],
                    route.getEnd()[0], route.getEnd()[1],
                    route.getDistance(),
                    route.getProfile())
                )
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
