package heigit.ors.routing.pathprocessors;

import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.parameters.ProfileParameters;
import heigit.ors.routing.parameters.VehicleParameters;


public class BordersExtractor {
    public enum Avoid { CONTROLLED, NONE, ALL };
    private VehicleParameters _vehicleParams;
    private BordersGraphStorage _storage;
    private int[] _avoidCountries;

    public BordersExtractor(BordersGraphStorage storage, ProfileParameters vehicleParams, int[] avoidCountries)
    {
        _storage = storage;

        _avoidCountries = avoidCountries;

        if (vehicleParams instanceof VehicleParameters)
            _vehicleParams = (VehicleParameters)vehicleParams;
    }

    public int getValue(int edgeId)
    {
        // Get the type of border
        return _storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE);
    }

    public boolean isBorder(int edgeId) {
        int type = _storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE);

        return (type == BordersGraphStorage.OPEN_BORDER || type == BordersGraphStorage.CONTROLLED_BORDER);
    }

    public boolean isControlledBorder(int edgeId) {
        return _storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE) == BordersGraphStorage.CONTROLLED_BORDER;
    }

    public boolean isOpenBorder(int edgeId) {
        return _storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE) == BordersGraphStorage.OPEN_BORDER;
    }

    public boolean restrictedCountry(int edgeId) {
        int startCountry = _storage.getEdgeValue(edgeId, BordersGraphStorage.Property.START);
        int endCountry = _storage.getEdgeValue(edgeId, BordersGraphStorage.Property.END);

        for(int i=0; i<_avoidCountries.length; i++) {
            if(startCountry == _avoidCountries[i] || endCountry == _avoidCountries[i] ) {
                return true;
            }
        }

        return false;
    }
}
