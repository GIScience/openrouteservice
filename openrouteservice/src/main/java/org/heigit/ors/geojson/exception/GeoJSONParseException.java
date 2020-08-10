package org.heigit.ors.geojson.exception;

public class GeoJSONParseException extends RuntimeException {
  public GeoJSONParseException() {
    super();
  }

  public GeoJSONParseException(String message) {
    super(message);
  }

  public GeoJSONParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public GeoJSONParseException(String message, Throwable cause, boolean enableSuppression, boolean writeStackTrace) {
    super(message, cause, enableSuppression, writeStackTrace);
  }

  public GeoJSONParseException(Throwable cause) {
    super(cause);
  }
}
