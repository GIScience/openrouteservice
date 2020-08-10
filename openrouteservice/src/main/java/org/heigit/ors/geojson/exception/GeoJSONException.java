package org.heigit.ors.geojson.exception;

public class GeoJSONException extends RuntimeException {
  public GeoJSONException() {
    super();
  }

  public GeoJSONException(String message) {
    super(message);
  }

  public GeoJSONException(String message, Throwable cause) {
    super(message, cause);
  }

  public GeoJSONException(String message, Throwable cause, boolean enableSuppression, boolean writeStackTrace) {
    super(message, cause, enableSuppression, writeStackTrace);
  }

  public GeoJSONException(Throwable cause) {
    super(cause);
  }
}
