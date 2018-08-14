package heigit.ors.controllers.responseHolders;

public abstract class RouteResponse {
    private Route route;

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
}
