package heigit.ors.routing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProfileWeightingCollection {
	protected List<ProfileWeighting> _weightings;

	public ProfileWeightingCollection()
	{
		_weightings = new ArrayList<ProfileWeighting>();
	}

	public void add(ProfileWeighting weighting)
	{
		if (_weightings == null)
			_weightings = new ArrayList<ProfileWeighting>();

		_weightings.add(weighting);
	}
	
	public Iterator<ProfileWeighting> getIterator()
	{
		return _weightings.iterator();
	}
	
	public int size()
	{
		return _weightings.size();
	}
}
