package heigit.ors.locations.providers;

import heigit.ors.locations.providers.LocationsDataProvider;

public class LocationsDataProviderItem 
{
	private LocationsDataProvider _provider;
	private boolean _isInitialized = false;

	public LocationsDataProviderItem(LocationsDataProvider provider)
	{
		_provider = provider;
	}

	public LocationsDataProvider getProvider()
	{
		return _provider;
	}

	public boolean getIsInitialized() {
		return _isInitialized;
	}

	public void setIsInitialized(boolean isInitialized) {
		_isInitialized = isInitialized;
	}
}