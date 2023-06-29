package org.heigit.ors.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;

@Configuration
@ConfigurationProperties(prefix = "endpoints")
public class EndpointsProperties {
    private EndpointDefaultProperties defaults;
    private EndpointRoutingProperties routing;
    private EndpointMatrixProperties matrix;
    private EndpointIsochroneProperties isochrone;

    public EndpointDefaultProperties getDefaults() {
        return defaults;
    }

    public void setDefaults(EndpointDefaultProperties defaults) {
        this.defaults = defaults;
    }

    public EndpointRoutingProperties getRouting() {
        return routing;
    }

    public void setRouting(EndpointRoutingProperties routing) {
        this.routing = routing;
    }

    public EndpointMatrixProperties getMatrix() {
        return matrix;
    }

    public void setMatrix(EndpointMatrixProperties matrix) {
        this.matrix = matrix;
    }

    public EndpointIsochroneProperties getIsochrone() {
        return isochrone;
    }

    public void setIsochrone(EndpointIsochroneProperties isochrone) {
        this.isochrone = isochrone;
    }

    public static class EndpointDefaultProperties {
        private String attribution;

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }
    }

    public static class EndpointRoutingProperties {
        private boolean enabled;
        private String attribution;
        private String routingName;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

        public String getRoutingName() {
            return routingName;
        }

        public void setRoutingName(String routingName) {
            this.routingName = routingName;
        }


    }

    public static class EndpointMatrixProperties {
        private boolean enabled;
        private String attribution;
        private int maximumRoutes = 2500;
        private int maximumRoutesFlexible = 25;
        private int maximumVisitedNodes = 100000;
        private double maximumSearchRadius = 2000;
        // TODO: this parameter is only used in a binary check for infinity (==-1);
        //       Can't we reduce it to a boolean "forbid_u_turns"?
        private double uTurnCost = INFINITE_U_TURN_COSTS;


        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

        public int getMaximumRoutes(boolean flexible) {
            return (flexible ? maximumRoutesFlexible : maximumRoutes);
        }

        public void setMaximumRoutes(int maximumRoutes) {
            this.maximumRoutes = maximumRoutes;
        }

        public void setMaximumRoutesFlexible(int maximumRoutesFlexible) {
            this.maximumRoutesFlexible = maximumRoutesFlexible;
        }

        public int getMaximumVisitedNodes() {
            return maximumVisitedNodes;
        }

        public void setMaximumVisitedNodes(int maximumVisitedNodes) {
            this.maximumVisitedNodes = maximumVisitedNodes;
        }

        public double getMaximumSearchRadius() {
            return maximumSearchRadius;
        }

        public void setMaximumSearchRadius(double maximumSearchRadius) {
            this.maximumSearchRadius = maximumSearchRadius;
        }

        public double getUTurnCost() {
            return uTurnCost;
        }

        public void setUTurnCost(double uTurnCosts) {
            this.uTurnCost = uTurnCosts;
        }
    }

    public static class EndpointIsochroneProperties {
        private boolean enabled;
        private String attribution;
        private int maximumLocations;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

        public int getMaximumLocations() {
            return maximumLocations;
        }

        public void setMaximumLocations(int maximumLocations) {
            this.maximumLocations = maximumLocations;
        }
    }
}
