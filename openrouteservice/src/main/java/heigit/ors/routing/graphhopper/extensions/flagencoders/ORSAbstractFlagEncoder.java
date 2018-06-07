/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.routing.util.AbstractFlagEncoder;

public abstract class ORSAbstractFlagEncoder extends AbstractFlagEncoder {

    protected SpeedLimitHandler _speedLimitHandler;

	protected ORSAbstractFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
		super(speedBits, speedFactor, maxTurnCosts);
	}

	double secondsTo100KmpH() {
	    return 20;
    }

    double accelerationKmpHpS() {
	    return 100.0 / secondsTo100KmpH();
    }

	double adjustSpeedForAcceleration(double distance, double maximumSpeedInKmph) {
		// We only want to perform the adjustment if the road is a slower speed - main roads shouldnt be affected as much due to less junctions and changes in direction
		if(maximumSpeedInKmph < 80) {
			if (distance <= 0) {
				return maximumSpeedInKmph;
			}

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
		} else {;
			return maximumSpeedInKmph;
		}
	}

	private double durationToMaxSpeed(double initialSpeedInKmph, double maxSpeedInKmph) {
		return  (maxSpeedInKmph - initialSpeedInKmph) / accelerationKmpHpS();
	}

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

	private double convertKmphToMps(double speedInKmph) {
		return (speedInKmph * 1000) / 3600;
	}

	private double convertMpsToKmph(double speedInMps) {
		return (3600 * speedInMps) / 1000;
	}
}
