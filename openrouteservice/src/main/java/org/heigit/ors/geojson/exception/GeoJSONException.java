package org.heigit.ors.geojson.exception;

/**
 * Extends {@link RuntimeException} with a separate exception type for all GeoJSON paring exceptions.
 */
public class GeoJSONException extends RuntimeException {
  /**
   * Constructs a plain GeoJSONException.
   * @see RuntimeException#RuntimeException()
   */
  public GeoJSONException() {
    super();
  }

  /**
   * Constructs a GeoJSONException with a given message.
   * @see RuntimeException#RuntimeException(String)
   */
  public GeoJSONException(String message) {
    super(message);
  }

  /**
   * Constructs a GeoJSONException with a given message, and cause.
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public GeoJSONException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a GeoJSONException with a given message, cause, and options.
   * @see RuntimeException#RuntimeException(String, Throwable, boolean, boolean)
   */
  public GeoJSONException(String message, Throwable cause, boolean enableSuppression, boolean writeStackTrace) {
    super(message, cause, enableSuppression, writeStackTrace);
  }

  /**
   * Constructs a GeoJSONException with a given cause.
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public GeoJSONException(Throwable cause) {
    super(cause);
  }
}
