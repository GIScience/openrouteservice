package org.heigit.ors.config;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnginePropertiesTest {

    EngineProperties enginePropertiesTest;

    @BeforeEach
    void setUp() {
        enginePropertiesTest = new EngineProperties();

        enginePropertiesTest.getProfileDefault().setEnabled(false);
        enginePropertiesTest.getProfileDefault().setSourceFile(Path.of("/path/to/source/file"));
        enginePropertiesTest.getProfileDefault().setGraphPath(Path.of("/path/to/graphs"));
        enginePropertiesTest.getProfileDefault().getPreparation().setMinNetworkSize(300);
        enginePropertiesTest.getProfileDefault().getPreparation().getMethods().getLm().setEnabled(false);
        enginePropertiesTest.getProfileDefault().getPreparation().getMethods().getLm().setWeightings("shortest");
        enginePropertiesTest.getProfileDefault().getPreparation().getMethods().getLm().setLandmarks(2);
        enginePropertiesTest.getProfileDefault().getExecution().getMethods().getLm().setActiveLandmarks(2);
        Map<String, ExtendedStorageProperties> extStoragesDef = new LinkedHashMap<>();
        ExtendedStorageProperties extWayCategoryDefault = new ExtendedStorageProperties();
        extWayCategoryDefault.setEnabled(true);
        extStoragesDef.put("WayCategory", extWayCategoryDefault);
        ExtendedStorageProperties extGreenIndexDefault = new ExtendedStorageProperties();
        extGreenIndexDefault.setEnabled(true);
        extGreenIndexDefault.setFilepath(Path.of("/path/to/file.csv"));
        extStoragesDef.put("GreenIndex", extGreenIndexDefault);
        enginePropertiesTest.getProfileDefault().setExtStorages(extStoragesDef);

        ProfileProperties carProfile = new ProfileProperties();
        carProfile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        carProfile.getPreparation().setMinNetworkSize(300);
        carProfile.getPreparation().getMethods().getLm().setEnabled(true);
        carProfile.getPreparation().getMethods().getLm().setThreads(5);
        carProfile.getExecution().getMethods().getLm().setActiveLandmarks(2);
        enginePropertiesTest.getProfiles().put("car", carProfile);

        ProfileProperties hgvProfile = new ProfileProperties();
        hgvProfile.setEncoderName(EncoderNameEnum.DRIVING_HGV);
        hgvProfile.getPreparation().setMinNetworkSize(900);
        hgvProfile.getPreparation().getMethods().getLm().setEnabled(true);
        Map<String, ExtendedStorageProperties> extStoragesHgv = new LinkedHashMap<>();
        ExtendedStorageProperties extHeavyVehicle = new ExtendedStorageProperties();
        extHeavyVehicle.setRestrictions(true);
        extStoragesHgv.put("HeavyVehicle", extHeavyVehicle);
        hgvProfile.setExtStorages(extStoragesHgv);
        enginePropertiesTest.getProfiles().put("hgv", hgvProfile);

        ProfileProperties carCustomProfile = new ProfileProperties();
        carCustomProfile.setEnabled(true);
        carCustomProfile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        carCustomProfile.getPreparation().setMinNetworkSize(900);
        enginePropertiesTest.getProfiles().put("car-custom", carCustomProfile);

        ProfileProperties carCustom2Profile = new ProfileProperties();
        carCustom2Profile.setEnabled(false);
        carCustom2Profile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        enginePropertiesTest.getProfiles().put("car-custom2", carCustom2Profile);
    }

    @Test
    void getActiveProfilesReturnsEnabledProfilesWhenProfileDefaultIsNotEnabled() {
        Map<String, ProfileProperties> activeProfiles = enginePropertiesTest.getInitializedActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.containsKey("car"));
        assertFalse(activeProfiles.containsKey("hgv"));
        assertTrue(activeProfiles.containsKey("car-custom"));
        assertFalse(activeProfiles.containsKey("car-custom2"));
        assertEquals("car-custom", activeProfiles.get("car-custom").getProfileName());
    }

    @Test
    void getActiveProfilesReturnsCorrectProfilesWhenProfileDefaultIsEnabled() {
        enginePropertiesTest.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = enginePropertiesTest.getInitializedActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
        assertTrue(activeProfiles.containsKey("car"));
        assertTrue(activeProfiles.containsKey("hgv"));
        assertTrue(activeProfiles.containsKey("car-custom"));
        assertFalse(activeProfiles.containsKey("car-custom2"));
        assertEquals("car", activeProfiles.get("car").getProfileName());
        assertEquals("hgv", activeProfiles.get("hgv").getProfileName());
        assertEquals("car-custom", activeProfiles.get("car-custom").getProfileName());
    }
}