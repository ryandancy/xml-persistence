package ca.keal.persistence;

/**
 * A strategy for persisting an object. {@code PersistenceStrategy<T>}s take objects of type {@code T} and transform
 * them into {@link PersistedElement}s, modifying the {@link ToplevelList} appropriately.
 * 
 * The relevant {@code PersistenceStrategy} will be called for all persisting of fields of the relevant type
 * marked @{@link Persist}. The class is given in the constructor.
 * 
 * @param <T> The type of the object that this strategy may persist. This is given as a {@code Class<T>} in the
 * constructor and may be retrieved via {@link #getPersistingClass()}.
 */
abstract class PersistenceStrategy<T> {
  
  /** The class persisted by this strategy. */
  private final Class<T> cls;
  
  /**
   * Create a new instance of this {@link PersistenceStrategy} with the class object of the class being persisted by
   * this strategy.
   * @param cls The class object of the class this strategy may persist.
   * @throws NullPointerException If {@code cls} is {@code null}.
   */
  public PersistenceStrategy(Class<T> cls) {
    if (cls == null) {
      throw new NullPointerException("Strategy cannot have a null class");
    }
    this.cls = cls;
  }
  
  /**
   * @return The class that can be persisted by this strategy.
   */
  public Class<T> getPersistingClass() {
    return cls;
  }
  
  /**
   * Persist the object of the class returned by {@link #getPersistingClass()} to a {@link PersistedElement} given
   * its @{@link Persist} annotation. If any new {@link ToplevelElement}s are generated by persisting the element,
   * the provided {@link ToplevelList} must be modified using {@link ToplevelList#addElement}.
   * 
   * @param toplevelList The global (for this persisting) {@link ToplevelList} of {@link ToplevelElement}s. Modify this
   *  when a new {@link ToplevelElement} is generated.
   * @param persistAnno The @{@link Persist} annotation applied to {@code toPersist}.
   * @param toPersist The object to persist. An instance of the class returned by {@link #getPersistingClass()}.
   * @return A {@link PersistedElement} representing the additional element that must be added to the
   *  {@link ParentElement}.
   * @throws NullPointerException If any parameter is {@code null}.
   * @throws PersistenceException If an error is encountered when persisting the object.
   */
  public abstract PersistedElement persist(ToplevelList toplevelList, Persist persistAnno, T toPersist);
  
}
