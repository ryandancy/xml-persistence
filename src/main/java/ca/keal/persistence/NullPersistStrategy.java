package ca.keal.persistence;

/**
 * A {@link PersistenceStrategy} which persists null objects. This should only be done in the special case where the
 * object to be persisted is null.
 * @param <T> The type of (null) object to be persisted.
 */
class NullPersistStrategy<T> extends PersistenceStrategy<T> {
  
  /** Create a new {@link NullPersistStrategy} persisting the given class. */
  public NullPersistStrategy(Class<T> cls) {
    super(cls);
  }
  
  /**
   * Return a {@link NullElement} representing {@code toPersist}.
   * @throws IllegalArgumentException If {@code toPersist} is not null.
   * @throws PersistenceException If {@code persistAnno.value()} is not a valid XML tag name.
   */
  @Override
  public PersistedElement persist(PersistingState state, Persist persistAnno, T toPersist) {
    if (toPersist != null) {
      throw new IllegalArgumentException("NullPersistStrategy can only persist null objects - attempting to persist '"
         + toPersist.toString() + "'.");
    }
    
    return new NullElement(persistAnno.value());
  }
  
}
