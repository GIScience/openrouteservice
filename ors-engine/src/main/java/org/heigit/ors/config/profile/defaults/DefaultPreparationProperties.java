package org.heigit.ors.config.profile.defaults;

import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.PreparationProperties;

@Setter
@Getter
public class DefaultPreparationProperties extends PreparationProperties {

    public DefaultPreparationProperties() {
        super();
        setMinNetworkSize(200);
        setMinOneWayNetworkSize(200);

        setMethods(new MethodsProperties(true));
        getMethods().getCh().setEnabled(false);
        getMethods().getCh().setWeightings("fastest");
        getMethods().getCh().setThreads(2);

        getMethods().getLm().setEnabled(true);
        getMethods().getLm().setThreads(2);
        getMethods().getLm().setWeightings("recommended,shortest");
        getMethods().getLm().setLandmarks(16);

        getMethods().getCore().setEnabled(false);
        getMethods().getCore().setThreads(2);
        getMethods().getCore().setWeightings("fastest,shortest");
        getMethods().getCore().setLandmarks(64);
        getMethods().getCore().setLmsets("highways;allow_all");

        getMethods().getFastisochrones().setEnabled(false);
        getMethods().getFastisochrones().setThreads(2);
        getMethods().getFastisochrones().setWeightings("recommended,shortest");
    }

    public DefaultPreparationProperties(EncoderNameEnum encoderName) {
        this();
        if (encoderName == null) {
            encoderName = EncoderNameEnum.UNKNOWN;
        }

        switch (encoderName) {
            case DRIVING_CAR -> {
                setMinNetworkSize(200);
                getMethods().getCh().setEnabled(true);
                getMethods().getCh().setWeightings("fastest");
                getMethods().getLm().setEnabled(false);
                getMethods().getLm().setWeightings("fastest,shortest");
                getMethods().getLm().setLandmarks(16);
                getMethods().getCore().setEnabled(true);
                getMethods().getCore().setWeightings("fastest,shortest");
                getMethods().getCore().setLandmarks(64);
                getMethods().getCore().setLmsets("highways;allow_all");
            }
            case DRIVING_HGV -> {
                setMinNetworkSize(200);
                getMethods().getCh().setEnabled(true);
                getMethods().getCh().setWeightings("recommended");
                getMethods().getCore().setEnabled(true);
                getMethods().getCore().setWeightings("recommended,shortest");
                getMethods().getCore().setLandmarks(64);
                getMethods().getCore().setLmsets("highways;allow_all");
            }
            default -> {
            }
        }
    }
}
