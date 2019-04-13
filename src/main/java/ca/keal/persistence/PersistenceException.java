package ca.keal.persistence;

/**
 * A runtime exception thrown when an attempt at persisting an object and its attached hierarchy fails.
 */
public class PersistenceException extends RuntimeException {
  
  public PersistenceException() {}
  
  /** {@inheritDoc} */
  public PersistenceException(String message) {
    super(message);
  }
  
  /** {@inheritDoc} */
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
  
  /** {@inheritDoc} */
  public PersistenceException(Throwable cause) {
    super(cause);
  }
  
}
