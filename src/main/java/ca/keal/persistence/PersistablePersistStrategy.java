package ca.keal.persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PersistenceStrategy} which persists objects marked @{@link Persistable}. This strategy will persist each
 * field in the given object, selecting and calling a {@link PersistenceStrategy} for each field. See
 * {@link #persist(ToplevelList, Persist, Object)} for a more specific description of this strategy's functioning.
 * 
 * @param <T> The class which may be persisted by this strategy.
 * @see #persist(ToplevelList, Persist, Object)
 */
// TODO this currently cannot handle persisting null fields - implement this -> use an attribute
public class PersistablePersistStrategy<T> extends PersistenceStrategy<T> {
  
  /** Create a new {@link PersistablePersistStrategy} persisting the specified class. */
  public PersistablePersistStrategy(Class<T> cls) {
    super(cls);
  }
  
  /**
   * <p>Persist {@code toPersist}. This is done by choosing an appropriate {@link PersistenceStrategy} for each field in
   * the class marked @{@link Persist}: if the field is a primitive or a {@code String},
   * {@link PrimitivePersistStrategy} is used; else, {@link PersistablePersistStrategy} is used.</p>
   * 
   * <p>What is done with the element containing the persisted versions of each field depends on whether the class
   * returned by {@link #getPersistingClass()} is marked {@code @Persistable(toplevel=true)} or not. (It must be
   * marked @{@link Persistable}.)</p>
   * 
   * <p>If it is <b>not</b> toplevel, the persisted fields are placed in a {@link ParentElement} with the tag given by
   * {@code persistAnno.value()}. This {@link ParentElement} is returned.</p>
   * 
   * <p>If it <b>is</b> toplevel, the persisted fields are placed in a {@link ToplevelElement} which is then added to
   * the {@code toplevelList} passed in. The {@link ToplevelElement} has the tag given by the {@code tag} field of
   * the @{@link Persistable} annotation on the persisting class and its {@code id} attribute is given by the value of
   * the field given by the @{@link Persistable} annotation's {@code idField} value. Then, that same ID value is used
   * as the text content of a {@link TextElement} bearing the tag given by {@code persistAnno.value()}. The
   * {@link TextElement} is returned as a reference to the element in the {@code toplevelList}.</p>
   * 
   * @param toplevelList The global (for this persisting) {@link ToplevelList} of {@link ToplevelElement}s.
   * @param persistAnno The @{@link Persist} annotation applied to {@code toPersist}.
   * @param toPersist The object to persist. An instance of the class returned by {@link #getPersistingClass()}.
   * @return Either a {@link ParentElement} containing persisted representations of each field marked @{@link Persist}
   *  in {@code toPersist} or a {@link TextElement} containing a reference to one in the {@code toplevelList} as
   *  described above.
   * @throws PersistenceException If there is a problem persisting a field.
   * @throws NullPointerException If any parameter is {@code null}. Notably, this includes {@code toPersist}, so
   *  persisting null fields is not currently supported.
   */
  @Override
  public PersistedElement persist(ToplevelList toplevelList, Persist persistAnno, T toPersist) {
    Persistable persistable = PersistenceUtil.verifyAndGetPersistable(getPersistingClass());
    if (persistable.toplevel()) {
      return persistToplevel(toplevelList, persistAnno, persistable, toPersist);
    } else {
      return persistNonToplevel(toplevelList, persistAnno, toPersist);
    }
  }
  
  /**
   * The implementation of {@link #persist(ToplevelList, Persist, Object)} for when the persistable object is toplevel.
   * @see #persist(ToplevelList, Persist, Object)
   */
  private PersistedElement persistToplevel(ToplevelList toplevelList, Persist persistAnno,
                                           Persistable persistable, T toPersist) {
    // Extract the id from the idField
    String id;
    try {
      Field idField = getPersistingClass().getDeclaredField(persistable.idField());
      idField.setAccessible(true);
      id = idField.get(toPersist).toString();
    } catch (NoSuchFieldException e) {
      // Yeah this never happens, this case was caught in verifyAndGetPersistable()
      System.err.println("ERROR: THIS SHOULD NOT HAPPEN. The idField specified in the @Persistable annotation of '"
          + getPersistingClass().getCanonicalName() + "' does not exist despite passing previous tests. There is a "
          + "bug in PersistenceUtil.verifyAndGetPersistable() - please tell developer.");
      throw new PersistenceException("This should not happen. idField of '" + getPersistingClass().getCanonicalName()
          + "' does not exist despite being verified previously.");
    } catch (IllegalAccessException e) {
      throw new PersistenceException("The specified idField, '" + persistable.idField() + "' in '"
          + getPersistingClass().getCanonicalName() + "' is inaccessible and cannot be persisted.");
    }
    
    // Generate a new toplevel element for it only if it isn't persisted already
    if (!toplevelList.contains(persistable.tag(), id)) {
      ToplevelElement toplevelElement = new ToplevelElement(persistable.tag(), id);
      // We add the element before we populate it so that other elements can refer to this element's toplevel id
      // (i.e. we're reserving this element's place in the toplevel list)
      toplevelList.addElement(toplevelElement);
      populateStructure(toplevelList, toplevelElement, toPersist);
    }
    
    // Return a reference to the toplevel element
    return new TextElement(persistAnno.value(), id);
  }
  
  /**
   * The implementation of {@link #persist(ToplevelList, Persist, Object)} for when the object is not toplevel.
   * @see #persist(ToplevelList, Persist, Object)
   */
  private PersistedElement persistNonToplevel(ToplevelList toplevelList, Persist persistAnno, T toPersist) {
    // Generate and return a new element
    ParentElement element = new ParentElement(persistAnno.value());
    populateStructure(toplevelList, element, toPersist);
    return element;
  }
  
  /**
   * Get all of the declared fields in the entire hierarchy of getPersistingClass(), except for java.lang.Object,
   * set accessible and return. We need declared fields so as to be able to access non-public fields.
   */
  private List<Field> getAllDeclaredFields() {
    List<Field> fields = new ArrayList<>();
    Class<?> currentClass = getPersistingClass();
    while (!currentClass.equals(Object.class)) {
      for (Field field : currentClass.getDeclaredFields()) {
        field.setAccessible(true);
        fields.add(field);
      }
      currentClass = currentClass.getSuperclass();
    }
    return fields;
  }
  
  /**
   * Populate {@code parent} with the persisted representations of each field in {@code toPersist}. Also make sure that
   * there are no duplicate @Persist values, as that would make it impossible to regenerate the class structure.
   */
  private void populateStructure(ToplevelList toplevelList, ParentElement parent, T toPersist) {
    List<String> persistValuesSeen = new ArrayList<>();
    
    for (Field field : getAllDeclaredFields()) {
      Persist persistAnno = field.getAnnotation(Persist.class);
      if (persistAnno != null) {
        if (persistValuesSeen.contains(persistAnno.value())) {
          throw new PersistenceException("Duplicate @Persist values are not allowed in one class: '"
            + persistAnno.value() + "' seen twice in '" + getPersistingClass().getCanonicalName() + "'.");
        }
        
        PersistedElement child = callStrategy(field.getType(), field, toplevelList, persistAnno, toPersist);
        parent.addChild(child);
        persistValuesSeen.add(persistAnno.value());
      }
    }
  }
  
  /** Call the appropriate {@link PersistenceStrategy} given the field. This exists for generics reasons. */
  private <F> PersistedElement callStrategy(Class<F> fieldCls, Field field, ToplevelList toplevelList,
                                            Persist persistAnno, T toPersist) {
    try {
      PersistenceStrategy<F> strategy = pickStrategy(fieldCls);
      //noinspection unchecked
      return strategy.persist(toplevelList, persistAnno, (F) field.get(toPersist));
    } catch (IllegalAccessException e) {
      throw new PersistenceException("Cannot persist field protected by access control: " + field.getName()
        + " in " + field.getDeclaringClass());
    }
  }
  
  /** Pick an appropriate {@link PersistenceStrategy} to persist the given class and return it. */
  // TODO should this be moved somewhere other than here? Probably philosophically
  private static <R> PersistenceStrategy<R> pickStrategy(Class<R> cls) {
    if (cls.isPrimitive() || cls.equals(String.class)) {
      return new PrimitivePersistStrategy<>(cls);
    } else {
      return new PersistablePersistStrategy<>(cls);
    }
  }
  
}
