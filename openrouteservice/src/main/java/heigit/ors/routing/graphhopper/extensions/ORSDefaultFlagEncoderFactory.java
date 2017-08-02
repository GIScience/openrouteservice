package heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.DefaultFlagEncoderFactory;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FlagEncoderFactory;
import com.graphhopper.util.PMap;

import heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.CarOffRoadFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.CarTmcFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.CycleTourBikeFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.ElectroBikeFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.EmergencyFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.HikingFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.SafetyBikeFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;

public class ORSDefaultFlagEncoderFactory extends DefaultFlagEncoderFactory implements FlagEncoderFactory {
	 private static final String CAR = "car";
	 private static final String CAROFFROAD = "caroffroad";
	 private static final String SAFETYBIKE = "safetybike";
	 private static final String ELECTROBIKE = "electrobike";
	 private static final String CYCLETOURBIKE = "cycletourbike";
	 private static final String HIKING = "hiking";
	 private static final String WHEELCHAIR = "wheelchair";
	 private static final String HEAVYVEHICLE = "heavyvehicle";
	 private static final String CARTMC = "cartmc";
	 private static final String EMERGENCY = "emergency";
    
    @Override
    public FlagEncoder createFlagEncoder(String name, PMap configuration) {
    	if (name.equals(SAFETYBIKE))
            return new SafetyBikeFlagEncoder(configuration);
    	
    	if (name.equals(ELECTROBIKE))
            return new ElectroBikeFlagEncoder(configuration);

     	if (name.equals(CYCLETOURBIKE))
            return new CycleTourBikeFlagEncoder(configuration);
     	
    	if (name.equals(HIKING))
            return new HikingFlagEncoder(configuration);

    	if (name.equals(WHEELCHAIR))
            return new WheelchairFlagEncoder(configuration);
    	
    	if (name.equals(HEAVYVEHICLE))
            return new HeavyVehicleFlagEncoder(configuration);
    	
    	if (name.equals(CAR))
            return new CarFlagEncoder(configuration);

    	if (name.equals(CAROFFROAD))
            return new CarOffRoadFlagEncoder(configuration);
    	
    	if (name.equals(CARTMC))
            return new CarTmcFlagEncoder(configuration);
    	
    	if (name.equals(EMERGENCY))
            return new EmergencyFlagEncoder(configuration);

    	return super.createFlagEncoder(name, configuration);
    }
}
