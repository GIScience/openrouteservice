package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.api.util.AppConfigMigration;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphService {
    private static final Logger LOGGER = Logger.getLogger(AppConfigMigration.class.getName());

    public List<ORSGraphManager> graphManagers = new ArrayList<>();

    public void addGraphhopperLocation(ORSGraphManager orsGraphManager) {
        graphManagers.add(orsGraphManager);
    }

//    @Scheduled(cron = "${engine.graphservice.cron:0 * * * * *}")
////    @Scheduled(initialDelay = 60, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
//    public void ping() {
//        LOGGER.warn("remove this ping!");
//        for (ORSGraphManager orsGraphManager : graphManagers) {
//            orsGraphManager.downloadGraphIfNecessary();
//        }
//    }
}
