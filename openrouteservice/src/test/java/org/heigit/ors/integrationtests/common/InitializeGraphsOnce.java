package org.heigit.ors.integrationtests.common;

import org.heigit.ors.routing.RoutingProfileManager;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Order(Integer.MIN_VALUE) // Before even the context is created
public class InitializeGraphsOnce implements BeforeAllCallback {

	// This folder's name must also be configured in resources/ors-config.json:
	// "graphs_root_path": "graphs-integrationtests"
	private static final String GRAPHS_FOLDER = "graphs-integrationtests";
	private static final String GRAPHS_FOLDER_DELETED = "graphs-folder-deleted";

	@Override
	public void beforeAll(ExtensionContext extensionContext) {
		ExtensionContext.Store store = rootStore(extensionContext);
		boolean graphsFolderAlreadyDeleted = store.getOrDefault(GRAPHS_FOLDER_DELETED, Boolean.class, Boolean.FALSE);

		if (!graphsFolderAlreadyDeleted) {
			try {
				Path graphsFolder = Paths.get(GRAPHS_FOLDER);
				System.out.printf("Deleting folder %s%n", graphsFolder.toAbsolutePath());
				FileSystemUtils.deleteRecursively(graphsFolder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			store.put(GRAPHS_FOLDER_DELETED, true);
		}
		RoutingProfileManager.getInstance(); // Waiting for all graphs being built
	}

	private static ExtensionContext.Store rootStore(ExtensionContext extensionContext) {
		ExtensionContext.Store store = extensionContext.getRoot().getStore(ExtensionContext.Namespace.create(InitializeGraphsOnce.class));
		return store;
	}
}
