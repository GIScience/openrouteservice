package org.heigit.ors.config.defaults;

import org.heigit.ors.config.GraphManagementProperties;

public class DefaultGraphManagementProperties extends GraphManagementProperties {

    public DefaultGraphManagementProperties() {
        setActivationSchedule("0 0 0 31 2 *");
        setDownloadSchedule("0 0 0 31 2 *");
        setMaxBackups(0);
    }
}
