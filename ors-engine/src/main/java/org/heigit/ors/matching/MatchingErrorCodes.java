package org.heigit.ors.matching;

public class MatchingErrorCodes {

    //Keep in sync with documentation: error-codes.md

    public static final int BASE = 8000;
    public static final int INVALID_JSON_FORMAT = 8000;
    public static final int MISSING_PARAMETER = 8001;
    public static final int INVALID_PARAMETER_FORMAT = 8002;
    public static final int INVALID_PARAMETER_VALUE = 8003;
    public static final int PARAMETER_VALUE_EXCEEDS_MAXIMUM = 8004;

    public static final int UNSUPPORTED_EXPORT_FORMAT = 8007;

    public static final int POINT_NOT_FOUND = 8010;
    public static final int UNKNOWN_PARAMETER = 8011;

    public static final int UNKNOWN = 8099;

    private MatchingErrorCodes() {
    }

}
