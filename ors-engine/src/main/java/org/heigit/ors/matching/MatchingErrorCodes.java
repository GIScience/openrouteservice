package org.heigit.ors.matching;

public class MatchingErrorCodes {

    //Keep in sync with documentation: error-codes.md

    public static final int BASE = 9000;
    public static final int INVALID_JSON_FORMAT = 9000;
    public static final int MISSING_PARAMETER = 9001;
    public static final int INVALID_PARAMETER_FORMAT = 9002;
    public static final int INVALID_PARAMETER_VALUE = 9003;
    public static final int PARAMETER_VALUE_EXCEEDS_MAXIMUM = 9004;

    public static final int UNSUPPORTED_EXPORT_FORMAT = 9007;

    public static final int LINE_NOT_MATCHED = 9009;
    public static final int POINT_NOT_FOUND = 9010;
    public static final int UNKNOWN_PARAMETER = 9011;

    public static final int UNKNOWN = 9099;

    private MatchingErrorCodes() {
    }

}
