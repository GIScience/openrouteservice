package org.heigit.ors.routing;

public interface RouteRequestParameterNames {
    String PARAM_COORDINATES = "coordinates";
    String PARAM_PREFERENCE = "preference";
    String PARAM_FORMAT = "format";
    String PARAM_UNITS = "units";
    String PARAM_LANGUAGE = "language";
    String PARAM_GEOMETRY = "geometry";
    String PARAM_INSTRUCTIONS = "instructions";
    String PARAM_INSTRUCTIONS_FORMAT = "instructions_format";
    String PARAM_ROUNDABOUT_EXITS = "roundabout_exits";
    String PARAM_ATTRIBUTES = "attributes";
    String PARAM_MANEUVERS = "maneuvers";
    String PARAM_RADII = "radiuses";
    String PARAM_BEARINGS = "bearings";
    String PARAM_CONTINUE_STRAIGHT = "continue_straight";
    String PARAM_ELEVATION = "elevation";
    String PARAM_EXTRA_INFO = "extra_info";
    String PARAM_OPTIMIZED = "optimized";
    String PARAM_OPTIONS = "options";
    String PARAM_SUPPRESS_WARNINGS = "suppress_warnings";
    String PARAM_SIMPLIFY_GEOMETRY = "geometry_simplify";
    String PARAM_SKIP_SEGMENTS = "skip_segments";
    String PARAM_ALTERNATIVE_ROUTES = "alternative_routes";
    String PARAM_DEPARTURE = "departure";
    String PARAM_ARRIVAL = "arrival";
    String PARAM_MAXIMUM_SPEED = "maximum_speed";
    String PARAM_ROUND_TRIP_OPTIONS = "round_trip";
    String PARAM_AVOID_FEATURES = "avoid_features";
    String PARAM_AVOID_BORDERS = "avoid_borders";
    String PARAM_AVOID_COUNTRIES = "avoid_countries";
    String PARAM_VEHICLE_TYPE = "vehicle_type";
    String PARAM_PROFILE_PARAMS = "profile_params";
    String PARAM_AVOID_POLYGONS = "avoid_polygons";
    // Fields specific to GraphHopper GTFS
    String PARAM_SCHEDULE = "schedule";
    String PARAM_SCHEDULE_DURATION = "schedule_duration";
    String PARAM_SCHEDULE_ROWS = "schedule_rows";
    String PARAM_WALKING_TIME = "walking_time";
    String PARAM_IGNORE_TRANSFERS = "ignore_transfers";
}
