package heigit.ors.geocoding.geocoders;

public interface SearchBoundary {
   boolean contains(double lon, double lat);
}
