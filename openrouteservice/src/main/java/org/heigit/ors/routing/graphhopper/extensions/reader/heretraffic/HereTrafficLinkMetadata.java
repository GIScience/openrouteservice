package org.heigit.ors.routing.graphhopper.extensions.reader.heretraffic;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.opengis.feature.Property;

import java.util.Collection;


public class HereTrafficLinkMetadata {
    private static final Logger LOGGER = Logger.getLogger(HereTrafficLinkMetadata.class.getName());

    private HereTrafficEnums.LinkTravelDirection travelDirection;
    private HereTrafficEnums.FunctionalClass functionalClass = HereTrafficEnums.FunctionalClass.CLASS5;
    private HereTrafficEnums.NoYesEnum frontageRoad = HereTrafficEnums.NoYesEnum.NO;
    private HereTrafficEnums.NoYesEnum ramp = HereTrafficEnums.NoYesEnum.NO;
    private HereTrafficEnums.NoYesEnum roundabout = HereTrafficEnums.NoYesEnum.NO;
    private HereTrafficEnums.FerryType ferryType = HereTrafficEnums.FerryType.NO;
    private HereTrafficEnums.NoYesEnum specialTrafficFigure = HereTrafficEnums.NoYesEnum.NO;

    public HereTrafficLinkMetadata(Collection<Property> properties) {
        for (Property property : properties) {
            try {
                String propertyName = property.getName().toString();
                switch (propertyName) {
                    case "DIR_TRAVEL":
                        travelDirection = HereTrafficEnums.LinkTravelDirection.forValue(property.getValue().toString());
                        break;
                    case "FUNC_CLASS":
                        functionalClass = HereTrafficEnums.FunctionalClass.forValue(Integer.parseInt(property.getValue().toString()));
                        break;
                    case "FRONTAGE":
                        frontageRoad = HereTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "RAMP":
                        ramp = HereTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "ROUNDABOUT":
                        roundabout = HereTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "FERRY_TYPE":
                        ferryType = HereTrafficEnums.FerryType.forValue(property.getValue().toString().charAt(0));
                        break;
                    case "SPECTRFIG":
                        specialTrafficFigure = HereTrafficEnums.NoYesEnum.forValue(property.getValue().toString().charAt(0));
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
        return ferryType != HereTrafficEnums.FerryType.NO;
    }


    public boolean isRoundAbout() {
        return roundabout != HereTrafficEnums.NoYesEnum.NO;
    }

    public HereTrafficEnums.LinkTravelDirection getTravelDirection() {
        return this.travelDirection;
    }

    public HereTrafficEnums.FunctionalClass functionalClass() {
        return functionalClass;
    }

    public int getFunctionalClassWithRamp() {
        if (this.ramp == HereTrafficEnums.NoYesEnum.NO) {
            return this.functionalClass.getFunctionalClass();
        } else if (this.functionalClass == HereTrafficEnums.FunctionalClass.CLASS1) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS1LINK.value;
        } else if (this.functionalClass == HereTrafficEnums.FunctionalClass.CLASS2) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS2LINK.value;
        } else if (this.functionalClass == HereTrafficEnums.FunctionalClass.CLASS3) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS3LINK.value;
        } else if (this.functionalClass == HereTrafficEnums.FunctionalClass.CLASS4) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value;
        }
        return 0;
    }
}
