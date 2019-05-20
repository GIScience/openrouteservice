/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.util.BitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ORSAbstractFlagEncoder extends AbstractFlagEncoder {
	private final static Logger logger = LoggerFactory.getLogger(ORSAbstractFlagEncoder.class);

	protected boolean considerElevation = false;
	protected EncodedDoubleValue reverseSpeedEncoder;
	protected boolean blockByDefault = true;

	protected ORSAbstractFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
		super(speedBits, speedFactor, maxTurnCosts);
	}

	public void setConsiderElevation(boolean considerElevation) {
		this.considerElevation = considerElevation;
	}

	public abstract double getMeanSpeed();

	@Override
	public long reverseFlags(long flags) {
		flags = super.reverseFlags(flags);
		if (considerElevation) {
			// swap speeds
			double temp = reverseSpeedEncoder.getDoubleValue(flags);
			flags = setReverseSpeed(flags, speedEncoder.getDoubleValue(flags));
			flags = setSpeed(flags, temp);
		}
		return flags;
	}

	@Override
	protected long setLowSpeed(long flags, double speed, boolean reverse) {
		if (considerElevation) {
			if (reverse) {
				return setBool(reverseSpeedEncoder.setDoubleValue(flags, 0), K_BACKWARD, false);
			} else {
				return setBool(speedEncoder.setDoubleValue(flags, 0), K_FORWARD, false);
			}
		}
		return super.setLowSpeed(flags, speed, reverse);
	}

	@Override
	public long flagsDefault(boolean forward, boolean backward) {
		long flags = super.flagsDefault(forward, backward);
		if (considerElevation && backward) {
			flags = reverseSpeedEncoder.setDefaultValue(flags);
		}
		return flags;
	}

	@Override
	public long setReverseSpeed(long flags, double speed) {
		if (considerElevation) {
			// taken from GH Bike2WeightFlagEncoder.setReverseSpeed(...)
			if (speed < 0 || Double.isNaN(speed)) {
				throw new IllegalArgumentException("Speed cannot be negative: " + speed + ", flags:" + BitUtil.LITTLE.toBitString(flags));
			}
			if (speed < speedEncoder.getFactor() / 2) {
				return setLowSpeed(flags, speed, true);
			}
			if (speed > getMaxSpeed()) {
				speed = getMaxSpeed();
			}
			return reverseSpeedEncoder.setDoubleValue(flags, speed);
		} else {
			return setSpeed(flags, speed);
		}
	}

	@Override
	public double getReverseSpeed(long flags) {
		if (considerElevation) {
			return reverseSpeedEncoder.getDoubleValue(flags);
		}
		return getSpeed(flags);
	}

	@Override
	public long setProperties(double speed, boolean forward, boolean backward) {
		long flags = super.setAccess(setSpeed(0, speed), forward, backward);
		if (considerElevation && backward) {
			flags = setReverseSpeed(flags, speed);
		}
		return flags;
	}

	@Override
	public double getMaxSpeed(ReaderWay way) {
		return super.getMaxSpeed(way);
	}

	@Override
	protected double getFerrySpeed(ReaderWay way) {
		return getFerrySpeed(way, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	protected double getFerrySpeed(ReaderWay way, double unknownSpeed, double shortTripsSpeed, double longTripsSpeed) {
		// taken from GH AbstractFlagEncoder.getFerrySpeed(...),
		long duration = 0;

		try {
			// During the reader process we have converted the duration value into a artificial tag called "duration:seconds".
			duration = Long.parseLong(way.getTag("duration:seconds"));
		} catch (Exception ex) {
		}
		// seconds to hours
		double durationInHours = duration / 60d / 60d;
		// Check if our graphhopper specific artificially created estimated_distance way tag is present
		Number estimatedLength = way.getTag("estimated_distance", null);
		if (durationInHours > 0)
			try {
				if (estimatedLength != null) {
					double estimatedLengthInKm = estimatedLength.doubleValue() / 1000;
					// If duration AND distance is available we can calculate the speed more precisely
					// and set both speed to the same value. Factor 1.4 slower because of waiting time!
					double calculatedTripSpeed = estimatedLengthInKm / durationInHours / 1.4;
					// Plausibility check especially for the case of wrongly used PxM format with the intention to
					// specify the duration in minutes, but actually using months
					if (calculatedTripSpeed > 0.01d) {
						if (calculatedTripSpeed > getMaxSpeed()) {
							return getMaxSpeed();
						}
						// If the speed is lower than the speed we can store, we have to set it to the minSpeed, but > 0
						if (Math.round(calculatedTripSpeed) < speedEncoder.getFactor() / 2) {
							return speedEncoder.getFactor() / 2;
						}

						return Math.round(calculatedTripSpeed);
					} else {
						long lastId = way.getNodes().isEmpty() ? -1 : way.getNodes().get(way.getNodes().size() - 1);
						long firstId = way.getNodes().isEmpty() ? -1 : way.getNodes().get(0);
						if (firstId != lastId)
							logger.warn("Unrealistic long duration ignored in way with way ID=" + way.getId() + " : Duration tag value="
									+ way.getTag("duration") + " (=" + Math.round(duration / 60d) + " minutes)");
						durationInHours = 0;
					}
				}
			} catch (Exception ex) {
			}

		if (durationInHours == 0) {
			if(estimatedLength != null && estimatedLength.doubleValue() <= 300)
				return speedEncoder.getFactor() / 2;
			if(Integer.MIN_VALUE == unknownSpeed)
				return UNKNOWN_DURATION_FERRY_SPEED;
			return unknownSpeed;
		} else if (durationInHours > 1) {
			// lengthy ferries should be faster than short trip ferry
			if(Integer.MIN_VALUE == longTripsSpeed)
				return LONG_TRIP_FERRY_SPEED;
			return longTripsSpeed;
		} else {
			if(Integer.MIN_VALUE == shortTripsSpeed)
				return SHORT_TRIP_FERRY_SPEED;
			return shortTripsSpeed;
		}
	}

	@Override
	public void setBlockByDefault(boolean blockByDefault) {
		super.setBlockByDefault(blockByDefault);
		this.blockByDefault = blockByDefault;
	}

	public boolean isBlockByDefault() {
		return this.blockByDefault;
	}
}
