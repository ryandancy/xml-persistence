package ca.keal.persistence;

/**
 * A checked exception thrown when an attempt at regenerating an object's hierarchy from its persisted XML fails.
 */
public class RegenerationException extends Exception {
  
  public RegenerationException() {}
  
  /** {@inheritDoc} */
  public RegenerationException(String message) {
    super(message);
  }
  
  /** {@inheritDoc} */
  public RegenerationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  /** {@inheritDoc} */
  public RegenerationException(Throwable cause) {
    super(cause);
  }
  
}
