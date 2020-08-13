package org.heigit.ors.exceptions;

/**
 * Extends {@link Exception} with a separate exception type for all GeoJSON paring exceptions.
 */
public class AugmentationStorageException extends Exception {
  /**
   * Constructs a plain AugmentationStorageException.
   * @see Exception#Exception()
   */
  public AugmentationStorageException() {
    super();
  }

  /**
   * Constructs a AugmentationStorageException with a given message.
   * @see Exception#Exception(String)
   */
  public AugmentationStorageException(String message) {
    super(message);
  }

  /**
   * Constructs a AugmentationStorageException with a given message, and cause.
   * @see Exception#Exception(String, Throwable)
   */
  public AugmentationStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a AugmentationStorageException with a given message, cause, and options.
   * @see Exception#Exception(String, Throwable, boolean, boolean)
   */
  public AugmentationStorageException(String message, Throwable cause, boolean enableSuppression, boolean writeStackTrace) {
    super(message, cause, enableSuppression, writeStackTrace);
  }

  /**
   * Constructs a AugmentationStorageException with a given cause.
   * @see Exception#Exception(Throwable)
   */
  public AugmentationStorageException(Throwable cause) {
    super(cause);
  }
}
