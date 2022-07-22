package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.opengis.feature.Property;

import java.util.Collection;


public class TrafficLinkMetadata {
    private static final Logger LOGGER = Logger.getLogger(TrafficLinkMetadata.class.getName());

    private TrafficEnums.LinkTravelDirection travelDirection;
    private TrafficEnums.FunctionalClass functionalClass = TrafficEnums.FunctionalClass.CLASS5;
    private TrafficEnums.NoYesEnum frontageRoad = TrafficEnums.NoYesEnum.NO;
    private TrafficEnums.NoYesEnum ramp = TrafficEnums.NoYesEnum.NO;
    private TrafficEnums.NoYesEnum roundabout = TrafficEnums.NoYesEnum.NO;
    private TrafficEnums.FerryType ferryType = TrafficEnums.FerryType.NO;
    private TrafficEnums.NoYesEnum specialTrafficFigure = TrafficEnums.NoYesEnum.NO;

    public TrafficLinkMetadata(Collection<Property> properties) {
        for (Property property : properties) {
            try {
                String propertyName = property.getName().toString();
                switch (propertyName) {
                    case "DIR_TRAVEL":
                        travelDirection = TrafficEnums.LinkTravelDirection.forValue(property.getValue().toString());
                        break;
                    case "FUNC_CLASS":
                        functionalClass = TrafficEnums.FunctionalClass.forValue(Integer.parseInt(property.getValue().toString()));
                        break;
                    case "FRONTAGE":
                        frontageRoad = TrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "RAMP":
                        ramp = TrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "ROUNDABOUT":
                        roundabout = TrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "FERRY_TYPE":
                        ferryType = TrafficEnums.FerryType.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "SPECTRFIG":
                        specialTrafficFigure = TrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LOGGER.debug("Couldn't process property.");
            }
        }
    }

    public boolean isFerry() {
        return ferryType != TrafficEnums.FerryType.NO;
    }


    public boolean isRoundAbout() {
        return roundabout != TrafficEnums.NoYesEnum.NO;
    }

    public TrafficEnums.LinkTravelDirection getTravelDirection() {
        return this.travelDirection;
    }

    public TrafficEnums.FunctionalClass functionalClass() {
        return functionalClass;
    }

    public int getFunctionalClassWithRamp() {
        if (this.ramp == TrafficEnums.NoYesEnum.NO) {
            return this.functionalClass.getFunctionalClass();
        } else if (this.functionalClass == TrafficEnums.FunctionalClass.CLASS1) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS1LINK.value;
        } else if (this.functionalClass == TrafficEnums.FunctionalClass.CLASS2) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS2LINK.value;
        } else if (this.functionalClass == TrafficEnums.FunctionalClass.CLASS3) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS3LINK.value;
        } else if (this.functionalClass == TrafficEnums.FunctionalClass.CLASS4) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value;
        }
        return 0;
    }
}
