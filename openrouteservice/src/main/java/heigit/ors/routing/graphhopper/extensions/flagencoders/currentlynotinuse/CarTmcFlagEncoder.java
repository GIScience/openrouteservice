package heigit.ors.routing.graphhopper.extensions.flagencoders.currentlynotinuse;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;

/**
 * Defines bit layout for cars. (speed, access, ferries, ...)
 * <p>
 */
public class CarTmcFlagEncoder extends ExGhORSCarFlagEncoder {
	
	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	/**
	 * Should be only instantied via EncodingManager
	 */
	public CarTmcFlagEncoder() {
		this(5, 5, 0);
	}

    public CarTmcFlagEncoder(PMap configuration)
    {
		     this(configuration.getInt("speed_bits", 5),
		                configuration.getDouble("speed_factor", 5),
		                configuration.getBool("turn_costs", false) ? 3 : 0);
    }

	public CarTmcFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
		super(speedBits, speedFactor, maxTurnCosts);
		
		defaultSpeedMap.put("unclassified", 10);  
        defaultSpeedMap.put("residential", 10);
	}

	@Override
	public long acceptWay(ReaderWay way) {
		String highwayValue = way.getTag("highway");

		if (Helper.isEmpty(highwayValue))
			return 0;

		boolean accept = false;
		for (int i = 0; i < TMC_ROAD_TYPES.length; i++) {
			if (TMC_ROAD_TYPES[i].equalsIgnoreCase(highwayValue)) {
				accept = true;
				break;
			}
		}

		if (!accept)
			return 0;

		return super.acceptWay(way);
	}

	@Override
	public String toString() {
		return FlagEncoderNames.CARTMC;
	}
}
