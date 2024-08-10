package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;
import org.heigit.ors.config.profile.ProfileProperties;

import java.nio.file.Path;

public class RepoManagerTestHelper {


    public static EngineProperties createEnginePropertiesWithProfile(Path localGraphsRootPath,
                                                                     Path graphManagementRepositoryPath,
                                                                     String graphManagementRepositoryUrl,
                                                                     String graphManagementRepositoryName,
                                                                     String graphManagementRepositoryProfileGroup,
                                                                     String graphManagementGraphExtent,
                                                                     String graphManagementGraphVersion,
                                                                     int graphManagementMaxBackups,
                                                                     String profileName,
                                                                     ProfileProperties profileProperties) {
        EngineProperties engineProperties = createEngineProperties(localGraphsRootPath, graphManagementRepositoryPath, graphManagementRepositoryUrl, graphManagementRepositoryName, graphManagementRepositoryProfileGroup, graphManagementGraphExtent, graphManagementGraphVersion, graphManagementMaxBackups);
        engineProperties.getProfiles().put(profileName, profileProperties);
        return engineProperties;
    }

    public static EngineProperties createEngineProperties(Path localGraphsRootPath,
                                                          Path graphManagementRepositoryPath,
                                                          String graphManagementRepositoryUrl,
                                                          String graphManagementRepositoryName,
                                                          String graphManagementRepositoryProfileGroup,
                                                          String graphManagementGraphExtent,
                                                          String graphManagementGraphVersion,
                                                          int graphManagementMaxBackups) {

        GraphManagementProperties graphManagementProperties = new GraphManagementProperties();
        graphManagementProperties.setRepositoryUrl(graphManagementRepositoryUrl);
        if (graphManagementRepositoryPath != null) {
            graphManagementProperties.setRepositoryPath(graphManagementRepositoryPath.toFile().toString());
        }
        graphManagementProperties.setRepositoryName(graphManagementRepositoryName);
        graphManagementProperties.setRepositoryProfileGroup(graphManagementRepositoryProfileGroup);
        graphManagementProperties.setGraphExtent(graphManagementGraphExtent);
        graphManagementProperties.setGraphVersion(graphManagementGraphVersion);
        graphManagementProperties.setMaxBackups(graphManagementMaxBackups);

        EngineProperties engineProperties = new EngineProperties();
        engineProperties.setGraphManagement(graphManagementProperties);
        engineProperties.setGraphsRootPath(localGraphsRootPath);

        engineProperties.initialize();

        return engineProperties;
    }


}
