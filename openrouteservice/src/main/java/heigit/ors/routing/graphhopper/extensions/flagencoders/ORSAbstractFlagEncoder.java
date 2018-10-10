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

public abstract class ORSAbstractFlagEncoder extends AbstractFlagEncoder {

	private final double ACCELERATION_SPEED_CUTOFF_MAX = 80.0;
	private final double ACCELERATION_SPEED_CUTOFF_MIN = 20.0;
    protected SpeedLimitHandler _speedLimitHandler;

    private double accelerationModifier = 0.0;

	protected ORSAbstractFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
		super(speedBits, speedFactor, maxTurnCosts);
	}

	double addResedentialPenalty(double baseSpeed, ReaderWay way) {
		double speed = baseSpeed;
		if(way.hasTag("highway","residential")) {
			double estDist = way.getTag("estimated_distance", Double.MAX_VALUE);
			// take into account number of nodes to get an average distance between nodes
			double interimDistance = estDist;
			int interimNodes = way.getNodes().size() - 2;
			if(interimNodes > 0) {
				interimDistance = estDist/(interimNodes+1);
			}
			if(interimDistance < 100) {
				speed = speed * 0.5;
			}
		}

		return speed;
	}

	double averageSecondsTo100KmpH() { return 10; }

	/**
	 * Returns how many seconds it is assumed that this vehicle would reach 100 km/h taking into acocunt the acceleration modifier
	 *
	 * @return
	 */
	double secondsTo100KmpH() {
	    return averageSecondsTo100KmpH() + (accelerationModifier * averageSecondsTo100KmpH());
    }

	/**
	 * Returns the acceleration in KM per hour per second.
	 *
	 * @return
	 */
	double accelerationKmpHpS() {
	    return 100.0 / secondsTo100KmpH();
    }

	/**
	 * Adjust the maximum speed taking into account supposed acceleration on the segment. The method looks at acceleration
	 * along the way (assuming starting from 0km/h) and then uses the length to travel and the supposed maximum speed
	 * to determine an average speed for travelling along the whole segment.
	 *
	 * @param distance				How long the segment to travel along is
	 * @param maximumSpeedInKmph	The maximum speed that a vehicle can travel along this segment (usually the speed limit)
	 * @return
	 */
	double adjustSpeedForAcceleration(double distance, double maximumSpeedInKmph) {
		// We only want to perform the adjustment if the road is a slower speed - main roads shouldnt be affected as much due to less junctions and changes in direction
		if(maximumSpeedInKmph < ACCELERATION_SPEED_CUTOFF_MAX) {
			if (distance <= 0) {
				return maximumSpeedInKmph;
			}

			// slower roads can be assumed to have slower acceleration...

			double normalisedSpeed = maximumSpeedInKmph;
			if(normalisedSpeed < ACCELERATION_SPEED_CUTOFF_MIN)
				normalisedSpeed = ACCELERATION_SPEED_CUTOFF_MIN;

			normalisedSpeed = (normalisedSpeed -ACCELERATION_SPEED_CUTOFF_MIN) / (ACCELERATION_SPEED_CUTOFF_MAX-ACCELERATION_SPEED_CUTOFF_MIN);

			accelerationModifier = Math.pow(0.01, normalisedSpeed);

			double timeToMaxSpeed = durationToMaxSpeed(0, maximumSpeedInKmph);

			// We need to calculate how much distance is travelled in acceleration/deceleration phases
			double accelerationDistance = distanceTravelledInDuration(0, maximumSpeedInKmph, timeToMaxSpeed);

			double distanceAtMaxSpeed = distance - (accelerationDistance * 2); // Double the distance because of deceleration aswell

			double averageSpeed;

			if (distanceAtMaxSpeed < 0) {
				averageSpeed = convertMpsToKmph(distance / (durationToTravelDistance(0, maximumSpeedInKmph, distance / 2) * 2));
			} else {
				double timeAtMaxSpeed = distanceAtMaxSpeed / convertKmphToMps(maximumSpeedInKmph);
				double averageSpeedMps = distance / (timeToMaxSpeed*2 + timeAtMaxSpeed);

				averageSpeed = convertMpsToKmph(averageSpeedMps);
			}

			return averageSpeed;
		} else {
			return maximumSpeedInKmph;
		}
	}

	/**
	 * How many seconds does it take to reach maximum speed based on initial speed and acceleration.
	 *
	 * @param initialSpeedInKmph	How fast the vehicle is travelling at the start of the calculation
	 * @param maxSpeedInKmph		The target speed to be reached
	 * @return						How long it takes to reach the speed in seconds
	 */
	private double durationToMaxSpeed(double initialSpeedInKmph, double maxSpeedInKmph) {
		return  (maxSpeedInKmph - initialSpeedInKmph) / accelerationKmpHpS();
	}

	/**
	 * How long in seconds does it take to reach the intended distance based on the initial travelling speed and the
	 * maximum speed that can be travelled.
	 *
	 * @param initialSpeedInKmph	The speed of the vehicle when starting the calculation
	 * @param maxSpeedInKmph		The maximum speed the vehicle can travel at
	 * @param distanceInM			The target distance to be travelled
	 * @return						How long it takes in seconds to reach the target distance
	 */
	private double durationToTravelDistance(double initialSpeedInKmph, double maxSpeedInKmph, double distanceInM) {
		double secondsTravelled = 0;
		double distanceTravelled = 0;

		double currentSpeed = initialSpeedInKmph;

		while(currentSpeed < maxSpeedInKmph && distanceTravelled < distanceInM) {
			currentSpeed += accelerationKmpHpS();
			secondsTravelled += 1;
			distanceTravelled += convertKmphToMps(currentSpeed);
		}

		double distanceRemaining = distanceInM - distanceTravelled;

		if(distanceRemaining > 0) {
			secondsTravelled += (distanceRemaining / convertKmphToMps(maxSpeedInKmph));
		}

		return secondsTravelled;
	}

	/**
	 * How far can the vehicle travel in the specified time frame
	 *
	 * @param initialSpeedInKmph	The starting speed of the vehicle
	 * @param maxSpeedInKmph		The maximum travel speed
	 * @param duration				How long is the vehicle travelling for
	 * @return						The distance in metres that the vehicle travels in the specified time
	 */
	private double distanceTravelledInDuration(double initialSpeedInKmph, double maxSpeedInKmph, double duration) {
		double secondsTravelled = 0;
		double distanceTravelled = 0;
		double currentSpeed = initialSpeedInKmph;

		while(currentSpeed < maxSpeedInKmph && secondsTravelled < duration) {
			currentSpeed += accelerationKmpHpS();
			secondsTravelled += 1;
			distanceTravelled += convertKmphToMps(currentSpeed);
		}

		double secondsRemaining = duration - secondsTravelled;

		if(secondsRemaining > 0 ) {
			distanceTravelled += (secondsRemaining * convertKmphToMps(maxSpeedInKmph));
		}

		return distanceTravelled;
	}

	/**
	 * Convert kilometres per hour to metres per second
	 *
	 * @param speedInKmph	The speed to be converted in km per hour
	 * @return				The speed in metres per second
	 */
	private double convertKmphToMps(double speedInKmph) {
		return (speedInKmph * 1000) / 3600;
	}

	/**
	 * Convert metres per second to kilometres per hour
	 *
	 * @param speedInMps	The speed in metres per second
	 * @return				The speed in kilometres per hour
	 */
	private double convertMpsToKmph(double speedInMps) {
		return (3600 * speedInMps) / 1000;
	}
}
