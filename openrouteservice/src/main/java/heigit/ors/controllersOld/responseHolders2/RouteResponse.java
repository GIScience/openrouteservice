package heigit.ors.controllersOld.responseHolders2;

import heigit.ors.routing.RouteResult;

public abstract class RouteResponse {
    private Route route;

    private RouteResult routeResult;

    public RouteResponse() {
    }

    public abstract String getType();

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;

        this.route.generateRoute();
    }

    public void setRouteResult(RouteResult result) {
        this.routeResult = result;
    }

    public RouteResult getRouteResult() {
        return routeResult;
    }
}
