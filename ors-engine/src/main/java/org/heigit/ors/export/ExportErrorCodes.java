package org.heigit.ors.export;

public class ExportErrorCodes {
    public static final int BASE = 7000;
    public static final int INVALID_JSON_FORMAT = 7000;
    public static final int MISSING_PARAMETER = 7001;
    public static final int INVALID_PARAMETER_FORMAT = 7002;
    public static final int INVALID_PARAMETER_VALUE = 7003;
    public static final int UNKNOWN_PARAMETER = 7004;
    public static final int MISMATCHED_INPUT = 7005;
    public static final int UNSUPPORTED_EXPORT_FORMAT = 7006;
    public static final int UNKNOWN = 7099;
    private ExportErrorCodes() {}

}
