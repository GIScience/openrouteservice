package org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.opengis.feature.Property;

import java.util.Collection;


public class UberTrafficLinkMetadata {
    private static final Logger LOGGER = Logger.getLogger(UberTrafficLinkMetadata.class.getName());

    private UberTrafficEnums.LinkTravelDirection travelDirection;
    private UberTrafficEnums.FunctionalClass functionalClass = UberTrafficEnums.FunctionalClass.CLASS5;
    private UberTrafficEnums.NoYesEnum frontageRoad = UberTrafficEnums.NoYesEnum.NO;
    private UberTrafficEnums.NoYesEnum ramp = UberTrafficEnums.NoYesEnum.NO;
    private UberTrafficEnums.NoYesEnum roundabout = UberTrafficEnums.NoYesEnum.NO;
    private UberTrafficEnums.FerryType ferryType = UberTrafficEnums.FerryType.NO;
    private UberTrafficEnums.NoYesEnum specialTrafficFigure = UberTrafficEnums.NoYesEnum.NO;

    public UberTrafficLinkMetadata(Collection<Property> properties) {
        for (Property property : properties) {
            try {
                String propertyName = property.getName().toString();
                switch (propertyName) {
                    case "DIR_TRAVEL":
                        travelDirection = UberTrafficEnums.LinkTravelDirection.forValue(property.getValue().toString());
                        break;
                    case "FUNC_CLASS":
                        functionalClass = UberTrafficEnums.FunctionalClass.forValue(Integer.parseInt(property.getValue().toString()));
                        break;
                    case "FRONTAGE":
                        frontageRoad = UberTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "RAMP":
                        ramp = UberTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "ROUNDABOUT":
                        roundabout = UberTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "FERRY_TYPE":
                        ferryType = UberTrafficEnums.FerryType.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "SPECTRFIG":
                        specialTrafficFigure = UberTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
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
        return ferryType != UberTrafficEnums.FerryType.NO;
    }


    public boolean isRoundAbout() {
        return roundabout != UberTrafficEnums.NoYesEnum.NO;
    }

    public UberTrafficEnums.LinkTravelDirection getTravelDirection() {
        return this.travelDirection;
    }

    public UberTrafficEnums.FunctionalClass functionalClass() {
        return functionalClass;
    }

    public int getFunctionalClassWithRamp() {
        if (this.ramp == UberTrafficEnums.NoYesEnum.NO) {
            return this.functionalClass.getFunctionalClass();
        } else if (this.functionalClass == UberTrafficEnums.FunctionalClass.CLASS1) {
            return TrafficRelevantWayType.CLASS1LINK;
        } else if (this.functionalClass == UberTrafficEnums.FunctionalClass.CLASS2) {
            return TrafficRelevantWayType.CLASS2LINK;
        } else if (this.functionalClass == UberTrafficEnums.FunctionalClass.CLASS3) {
            return TrafficRelevantWayType.CLASS3LINK;
        } else if (this.functionalClass == UberTrafficEnums.FunctionalClass.CLASS4) {
            return TrafficRelevantWayType.CLASS4LINK;
        }
        return 0;
    }
}
