package ca.keal.persistence;

/**
 * A {@link PersistRegenStrategy} which persists null objects. This should only be done in the special case where the
 * object to be persisted is null.
 * @param <T> The type of (null) object to be persisted.
 */
class NullPRStrategy<T> extends PersistRegenStrategy<T> {
  
  /** Create a new {@link NullPRStrategy} persisting the given class. */
  public NullPRStrategy(Class<T> cls) {
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
      throw new IllegalArgumentException("NullPRStrategy can only persist null objects - attempting to persist '"
         + toPersist.toString() + "'.");
    }
    
    return new NullElement(persistAnno.value());
  }
  
  @Override
  public T regenerate(RegenState state, PersistedElement toRegen) throws RegenerationException {
    if (!(toRegen instanceof NullElement)) {
      throw new RegenerationException("NullPRStrategy can only regenerate from NullElements - attempted to regenerate " 
          + "from '" + toRegen.getTag() + "', a '" + toRegen.getClass().getCanonicalName() + "'.");
    }
    return null; // intentional; the corresponding object is indeed null.
  }
  
}
