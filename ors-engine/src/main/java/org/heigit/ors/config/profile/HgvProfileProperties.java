package org.heigit.ors.config.profile;

import org.heigit.ors.config.profile.storages.ExtendedStorageHeavyVehicle;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.heigit.ors.config.profile.storages.ExtendedStorageWaySurfaceType;

public class HgvProfileProperties extends ProfileProperties {
    public HgvProfileProperties() {
        super();
        this.setEncoderName("driving-hgv");
        getEncoderOptions().setTurnCosts(true);
        getEncoderOptions().setBlockFords(false);
        getEncoderOptions().setUseAcceleration(true);
        getPreparation().setMinNetworkSize(200);
        getPreparation().getMethods().getCh().setEnabled(true);
        getPreparation().getMethods().getCh().setThreads(1);
        getPreparation().getMethods().getCh().setWeightings("recommended");
        getPreparation().getMethods().getCore().setEnabled(true);
        getPreparation().getMethods().getCore().setThreads(1);
        getPreparation().getMethods().getCore().setWeightings("recommended,shortest");
        getPreparation().getMethods().getCore().setLandmarks(64);
        getPreparation().getMethods().getCore().setLmsets("highways;allow_all");
        getExecution().getMethods().getCore().setActiveLandmarks(6);
        getExtStorages().put("WayCategory", new ExtendedStorageWayCategory());
        getExtStorages().put("HeavyVehicle", new ExtendedStorageHeavyVehicle());
        getExtStorages().put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
    }
}
