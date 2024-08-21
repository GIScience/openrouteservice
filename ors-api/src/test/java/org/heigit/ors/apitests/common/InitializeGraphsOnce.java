package org.heigit.ors.apitests.common;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Order(Integer.MIN_VALUE) // Run before even spring context has been built
public class InitializeGraphsOnce implements BeforeAllCallback {

    private static final Logger LOGGER = Logger.getLogger(InitializeGraphsOnce.class.getName());

    // This folder's name must also be configured in resources/ors-config.json:
    // "graphs_root_path": "graphs-apitests"
    private static final String GRAPHS_FOLDER = "graphs-apitests";
    private static final String GRAPHS_FOLDER_DELETED = "graphs-folder-deleted";

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        ExtensionContext.Store store = rootStore(extensionContext);
        deleteGraphsFolderOncePerTestRun(store);
        SpringExtension.getApplicationContext(extensionContext);
        while (!RoutingProfileManagerStatus.isReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized static void deleteGraphsFolderOncePerTestRun(ExtensionContext.Store store) {
        boolean graphsFolderAlreadyDeleted = store.getOrDefault(GRAPHS_FOLDER_DELETED, Boolean.class, Boolean.FALSE);
        boolean ciPropertySet = System.getProperty("CI") != null && System.getProperty("CI").equalsIgnoreCase("true");
        boolean deleteGraphsFolder = !graphsFolderAlreadyDeleted && ciPropertySet;

        // Necessary to allow api tests, if already other spring boot tests have created profiles
        // RoutingProfileManager.destroyInstance();

        if (deleteGraphsFolder) {
            try {
                Path graphsFolder = Paths.get(GRAPHS_FOLDER);
                // Any lower level will not be displayed since ORS log configuration is not in place at this stage
                LOGGER.error("Deleting folder %s to enforce regeneration of graphs%n".formatted(graphsFolder.toAbsolutePath()));
                FileSystemUtils.deleteRecursively(graphsFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            store.put(GRAPHS_FOLDER_DELETED, true);
        }
    }

    private static ExtensionContext.Store rootStore(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(ExtensionContext.Namespace.create(InitializeGraphsOnce.class));
    }
}
