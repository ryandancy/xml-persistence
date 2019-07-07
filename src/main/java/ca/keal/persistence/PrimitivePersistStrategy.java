package ca.keal.persistence;

/**
 * A {@link PersistenceStrategy} which simply saves the value as a {@link TextElement}'s value using {@code toString()}
 * on the primitive passed in. This should be used for primitive and string fields.
 * @param <T> The type of the primitive that will be persisted with this strategy.
 */
// TODO do we even need to pass cls in here? It's not used
class PrimitivePersistStrategy<T> extends PersistenceStrategy<T> {
  
  /** Create a new {@link PrimitivePersistStrategy} persisting the specified class. */
  public PrimitivePersistStrategy(Class<T> cls) {
    super(cls);
  }
  
  /**
   * @return A simple {@link TextElement} with the tag of the @{@link Persist} annotation's {@code value} and the text
   *  being {@code toPersist.toString()}.
   * @see PersistenceStrategy#persist(PersistingState, Persist, Object)
   */
  @Override
  public PersistedElement persist(PersistingState state, Persist persistAnno, T toPersist) {
    return new TextElement(persistAnno.value(), toPersist.toString());
  }
  
}
