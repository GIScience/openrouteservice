package org.heigit.ors.coordinates_generator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RouteRepository {    
    private final Map<String, List<Route>> routesByProfile;
    private final Map<String, Set<Route>> uniqueRoutesByProfile;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected static final Logger LOGGER = LoggerFactory.getLogger(RouteRepository.class);
    
    public RouteRepository(Set<String> profiles) {
        routesByProfile = new HashMap<>();
        uniqueRoutesByProfile = new HashMap<>();
        
        for (String profile : profiles) {
            routesByProfile.put(profile, new ArrayList<>());
            uniqueRoutesByProfile.put(profile, new HashSet<>());
        }
    }
    
    public boolean addRoute(Route route) {
        String profile = route.getProfile();
        
        lock.writeLock().lock();
        try {
            if (uniqueRoutesByProfile.get(profile).add(route)) {
                routesByProfile.get(profile).add(route);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("Error adding route: {}", e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean addRouteIfUnique(Route route) {
        return addRoute(route);
    }
    
    public void clear() {
        lock.writeLock().lock();
        try {
            routesByProfile.values().forEach(List::clear);
            uniqueRoutesByProfile.values().forEach(Set::clear);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int getRouteCount(String profile) {
        lock.readLock().lock();
        try {
            return uniqueRoutesByProfile.get(profile).size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public int getTotalRouteCount() {
        lock.readLock().lock();
        try {
            return uniqueRoutesByProfile.values().stream()
                    .mapToInt(Set::size)
                    .sum();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean isProfileComplete(String profile, int requiredRoutes) {
        lock.readLock().lock();
        try {
            return uniqueRoutesByProfile.get(profile).size() >= requiredRoutes;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean areAllProfilesComplete(int requiredRoutes) {
        lock.readLock().lock();
        try {
            return uniqueRoutesByProfile.values().stream()
                    .allMatch(routes -> routes.size() >= requiredRoutes);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<Route> getAllRoutes(int limit) {
        lock.readLock().lock();
        try {
            List<Route> allRoutes = new ArrayList<>();
            routesByProfile.values().forEach(routes -> 
                routes.stream().limit(limit).forEach(allRoutes::add));
            return allRoutes;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void writeToCSV(String filePath) throws FileNotFoundException {
        lock.readLock().lock();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance,profile");
            
            routesByProfile.forEach((profile, routes) -> 
                routes.forEach(route -> pw.printf(Locale.US, "%f,%f,%f,%f,%.2f,%s%n",
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
    
    public void writeToCSV(String filePath, int limit) throws FileNotFoundException {
        lock.readLock().lock();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance,profile");
            
            routesByProfile.forEach((profile, routes) -> 
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
            return uniqueRoutesByProfile.entrySet().stream()
                .map(e -> String.format("%s: %d", e.getKey(), e.getValue().size()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        } finally {
            lock.readLock().unlock();
        }
    }
}
