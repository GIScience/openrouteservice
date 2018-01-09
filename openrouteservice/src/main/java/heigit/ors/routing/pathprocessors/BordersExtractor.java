package heigit.ors.routing.pathprocessors;

import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.parameters.ProfileParameters;
import heigit.ors.routing.parameters.VehicleParameters;

public class BordersExtractor {
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
