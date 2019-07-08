package ca.keal.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PersistRegenStrategy} which persists objects marked @{@link Persistable}. This strategy will persist each
 * field in the given object, selecting and calling a {@link PersistRegenStrategy} for each field. See
 * {@link #persist(PersistingState, Persist, Object)} for a more specific description of this strategy's functioning.
 * 
 * @param <T> The class which may be persisted by this strategy.
 * @see #persist(PersistingState, Persist, Object)
 */
public class PersistablePRStrategy<T> extends PersistRegenStrategy<T> {
  
  /** Create a new {@link PersistablePRStrategy} persisting the specified class. */
  public PersistablePRStrategy(Class<T> cls) {
    super(cls);
  }
  
  /**
   * <p>Persist {@code toPersist}. This is done by choosing an appropriate {@link PersistRegenStrategy} for each field in
   * the class marked @{@link Persist}: if the field is a primitive or a {@code String},
   * {@link PrimitivePRStrategy} is used; else, {@link PersistablePRStrategy} is used.</p>
   * 
   * <p>What is done with the element containing the persisted versions of each field depends on whether the class
   * returned by {@link #getPersistingClass()} is marked {@code @Persistable(toplevel=true)} or not. (It must be
   * marked @{@link Persistable}.)</p>
   * 
   * <p>If it is <b>not</b> toplevel, the persisted fields are placed in a {@link ParentElement} with the tag given by
   * {@code persistAnno.value()}. This {@link ParentElement} is returned.</p>
   * 
   * <p>If it <b>is</b> toplevel, the persisted fields are placed in a {@link ToplevelElement} which is then added to
   * the {@link ToplevelList} of the {@link PersistingState} passed in. The {@link ToplevelElement} has the tag given by
   * the {@code tag} field of the @{@link Persistable} annotation on the persisting class and its {@code id} attribute
   * is given by the value of the field given by the @{@link Persistable} annotation's {@code idField} value. Then, that
   * same ID value is used as the text content of a {@link TextElement} bearing the tag given by
   * {@code persistAnno.value()}. The {@link TextElement} is returned as a reference to the element in the
   * {@code toplevelList}.</p>
   * 
   * @param state The global (for this persisting) {@link PersistingState}s.
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
  public PersistedElement persist(PersistingState state, Persist persistAnno, T toPersist) {
    Persistable persistable = PersistenceUtil.verifyAndGetPersistable(getPersistingClass());
    if (persistable.toplevel()) {
      return persistToplevel(state, persistAnno, persistable, toPersist);
    } else {
      return persistNonToplevel(state, persistAnno, toPersist);
    }
  }
  
  /**
   * The implementation of {@link #persist(PersistingState, Persist, Object)} for when the object is toplevel.
   * @see #persist(PersistingState, Persist, Object)
   */
  private PersistedElement persistToplevel(PersistingState state, Persist persistAnno,
                                           Persistable persistable, T toPersist) {
    // Check/register the tag to avoid duplicate tags
    state.getDuplicateChecker().checkAndRegister(persistable.tag(), getPersistingClass());
    
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
    if (!state.getToplevelList().contains(persistable.tag(), id)) {
      ToplevelElement toplevelElement = new ToplevelElement(persistable.tag(), id);
      // We add the element before we populate it so that other elements can refer to this element's toplevel id
      // (i.e. we're reserving this element's place in the toplevel list)
      state.getToplevelList().addElement(toplevelElement);
      populateStructure(state, toplevelElement, toPersist);
    }
    
    // Return a reference to the toplevel element
    return new TextElement(persistAnno.value(), id);
  }
  
  /**
   * The implementation of {@link #persist(PersistingState, Persist, Object)} for when the object is not toplevel.
   * @see #persist(PersistingState, Persist, Object)
   */
  private PersistedElement persistNonToplevel(PersistingState state, Persist persistAnno, T toPersist) {
    // Generate and return a new element
    ParentElement element = new ParentElement(persistAnno.value());
    populateStructure(state, element, toPersist);
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
  private void populateStructure(PersistingState state, ParentElement parent, T toPersist) {
    List<String> persistValuesSeen = new ArrayList<>();
    
    for (Field field : getAllDeclaredFields()) {
      Persist persistAnno = field.getAnnotation(Persist.class);
      if (persistAnno != null) {
        if (persistValuesSeen.contains(persistAnno.value())) {
          throw new PersistenceException("Duplicate @Persist values are not allowed in one class: '"
            + persistAnno.value() + "' seen twice in '" + getPersistingClass().getCanonicalName() + "'.");
        }
        
        PersistedElement child = persistWithStrategy(field.getType(), field, state, persistAnno, toPersist);
        parent.addChild(child);
        persistValuesSeen.add(persistAnno.value());
      }
    }
  }
  
  /** Persist with the appropriate {@link PersistRegenStrategy} given the field. This exists for generics reasons. */
  private <F> PersistedElement persistWithStrategy(Class<F> fieldCls, Field field, PersistingState state,
                                                   Persist persistAnno, T toPersist) {
    try {
      @SuppressWarnings("unchecked") F value = (F) field.get(toPersist);
      PersistRegenStrategy<F> strategy = PersistenceUtil.pickStrategy(fieldCls, value);
      return strategy.persist(state, persistAnno, value);
    } catch (IllegalAccessException e) {
      throw new PersistenceException("Cannot persist field protected by access control: " + field.getName()
        + " in " + field.getDeclaringClass());
    }
  }
  
  /**
   * <p>Regenerate an instance of the class returned by {@link #getPersistingClass()} from XML elements.</p>
   * 
   * <p>There are three possible cases when this class is called. {@code toRegen} may be either a {@link TextElement}
   * representing a reference to a {@link ToplevelElement} (and therefore containing its ID), a {@link ToplevelElement},
   * or a regular {@link ParentElement}.</p>
   * 
   * <p>In the first case, we first check to see if the corresponding object has already been regenerated. If not, we
   * generate it from the corresponding {@link ToplevelElement}.</p>
   * 
   * <p>Then, in all cases, we iterate through the fields marked @{@link Persist} and regenerate each using an
   * appropriate strategy.</p>
   * 
   * @param state The global (for this regeneration) {@link RegenState}. Update its {@link RegenToplevelRegistry} with
   *  any new toplevel objects and retrieve new @{@link Persistable}-annotated elements from the {@link ToplevelList}.
   * @param toRegen The element from which to regenerate the object. Represents an instance of the class returned by
   *  {@link #getPersistingClass()}.
   * @return An object of {@link #getPersistingClass()} regenerated from {@code toRegen}.
   * @throws RegenerationException If an error is encountered in regenerating the object.
   */
  @Override
  public T regenerate(RegenState state, PersistedElement toRegen) throws RegenerationException {
    Persistable persistable = PersistenceUtil.verifyAndGetPersistable(getPersistingClass());
    
    // 3 cases: either it's a toplevel parent, an inner-level parent, or a toplevel reference
    
    if (toRegen instanceof TextElement) {
      // TextElement containing reference to ToplevelElement
      return regenerateReference(state, persistable, (TextElement) toRegen);
    } else if (toRegen instanceof ToplevelElement) {
      return regenerateToplevel(state, persistable, (ToplevelElement) toRegen);
    } else if (toRegen instanceof ParentElement) {
      return regenerateNonToplevel(state, persistable, (ParentElement) toRegen);
    } else {
      // Wrong kind of element
      throw new RegenerationException("PersistablePRStrategy cannot regenerate from "
          + toRegen.getClass().getCanonicalName());
    }
  }
  
  /** Regenerate from a {@link TextElement} containing a reference to a {@link ToplevelElement}. */
  private T regenerateReference(RegenState state, Persistable persistable, TextElement toRegen)
      throws RegenerationException {
    if (!persistable.toplevel()) {
      throw new RegenerationException("Encountered toplevel reference to non-toplevel persistable class");
    }
    
    String name = persistable.tag();
    String id = toRegen.getText();
    
    if (state.getToplevelRegistry().contains(name, id)) { // Try to find if we've already regenerated it
      return state.getToplevelRegistry().get(name, id);
    } else if (state.getToplevelList().contains(name, id)) { // Regenerate from the toplevel element
      ToplevelElement element = state.getToplevelList().getElement(name, id);
      return regenerateToplevel(state, persistable, element);
    } else {
      // Reference doesn't exist
      throw new RegenerationException("Toplevel element with tag name '" + name + "' and id '" + id
          + "' is referenced, but doesn't exist.");
    }
  }
  
  /** Regenerate from a {@link ToplevelElement}, registering the result. */
  private T regenerateToplevel(RegenState state, Persistable persistable, ToplevelElement toRegen)
      throws RegenerationException {
    if (!persistable.toplevel()) {
      throw new RegenerationException("Cannot regenerate toplevel element to non-toplevel @Persistable class");
    }
    
    // Instantiate the element
    T regenerated = instantiatePersistingClass(state);
    
    // Set the idField
    try {
      Field idField = getPersistingClass().getDeclaredField(persistable.idField());
      idField.setAccessible(true);
      idField.set(regenerated, conformIdTo(idField.getType(), toRegen.getId()));
    } catch (NoSuchFieldException e) {
      // once again, this was verified in verifyAndGetPersistable
      System.err.println("ERROR: THIS SHOULD NOT HAPPEN. The idField specified in the @Persistable annotation of '"
          + getPersistingClass().getCanonicalName() + "' does not exist despite passing previous tests. There is a "
          + "bug in PersistenceUtil.verifyAndGetPersistable() - please tell developer.");
      throw new RegenerationException("This should not happen. idField of '" + getPersistingClass().getCanonicalName()
          + "' does not exist despite being verified previously.");
    } catch (IllegalAccessException e) {
      throw new RegenerationException("Could not regenerate ID field '" + persistable.idField() + "' in "
          + getPersistingClass().getCanonicalName(), e);
    }
    
    // Register it
    state.getToplevelRegistry().register(toRegen.getTag(), toRegen.getId(), regenerated);
    
    // Fill it in
    // We register before we fill in the object so that if any field references this object, it can find it
    fillInRegenerated(state, regenerated, toRegen);
    
    return regenerated;
  }
  
  /** Regenerate from a non-toplevel {@link ParentElement}. */
  private T regenerateNonToplevel(RegenState state, Persistable persistable, ParentElement toRegen)
      throws RegenerationException {
    if (persistable.toplevel()) {
      throw new RegenerationException("Cannot regenerate non-toplevel element to toplevel @Persistable class");
    }
    
    // Just regenerate it
    T regenerated = instantiatePersistingClass(state);
    fillInRegenerated(state, regenerated, toRegen);
    return regenerated;
  }
  
  /** Regenerate each field marked @Persist inside {@code regenerated}. */
  private void fillInRegenerated(RegenState state, T regenerated, ParentElement toRegen) throws RegenerationException {
    // Persist each @Persist field; keep track of what child elements we use
    List<PersistedElement> usedChildren = new ArrayList<>();
    
    for (Field field : getAllDeclaredFields()) {
      Persist persistAnno = field.getAnnotation(Persist.class);
      if (persistAnno == null) continue;
      
      // Find the corresponding child element
      List<PersistedElement> childrenWithTag = toRegen.getChildrenByTag(persistAnno.value());
      
      if (childrenWithTag.isEmpty()) {
        throw new RegenerationException("Cannot find element with tag: '" + persistAnno.value() + "'.");
      } else if (childrenWithTag.size() > 1) {
        throw new RegenerationException("Multiple elements with same parent with tag: '" + persistAnno.value() + "'.");
      }
      
      PersistedElement child = childrenWithTag.get(0);
      usedChildren.add(child);
      
      // Regenerate the child into the object
      try {
        field.set(regenerated, PersistenceUtil.pickStrategy(field.getType(), child).regenerate(state, child));
      } catch (IllegalAccessException e) {
        throw new RegenerationException("Could not access field '" + field.getName() + "' in '"
            + getPersistingClass().getCanonicalName() + "' to regenerate it.", e);
      }
    }
    
    // Warn if there are unused fields
    for (PersistedElement child : toRegen.getChildren()) {
      if (!usedChildren.contains(child)) {
        System.out.println("Warning: <" + toRegen.getTag()
            + "> element contains child element that does not correspond to any @Persist-annotated field in "
            + getPersistingClass().getCanonicalName() + ": '" + child.getTag() + "'.");
      }
    }
  }
  
  /** Instantiate an instance of the class returned by {@link #getPersistingClass()} using Objenesis. */
  private T instantiatePersistingClass(RegenState state) {
    return state.getObjenesis().newInstance(getPersistingClass());
  }
  
  @SuppressWarnings("unchecked")
  private static <I> I conformIdTo(Class<I> cls, String id) throws RegenerationException {
    // Primitives + String
    if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
      return (I) Boolean.valueOf(Boolean.parseBoolean(id));
    } else if (cls.equals(byte.class) || cls.equals(Byte.class)) {
      return (I) new Byte(Byte.parseByte(id));
    } else if (cls.equals(char.class) || cls.equals(Character.class)) {
      return (I) new Character(id.charAt(0));
    } else if (cls.equals(short.class) || cls.equals(Short.class)) {
      return (I) new Short(Short.parseShort(id));
    } else if (cls.equals(int.class) || cls.equals(Integer.class)) {
      return (I) new Integer(Integer.parseInt(id));
    } else if (cls.equals(long.class) || cls.equals(Long.class)) {
      return (I) new Long(Long.parseLong(id));
    } else if (cls.equals(float.class) || cls.equals(Float.class)) {
      return (I) new Float(Float.parseFloat(id));
    } else if (cls.equals(double.class) || cls.equals(Double.class)) {
      return (I) new Double(Double.parseDouble(id));
    } else if (cls.equals(String.class)) {
      return (I) id;
    }
    
    // Look for fromString(String) method
    try {
      Method fromString = cls.getMethod("fromString", String.class);
      return (I) fromString.invoke(null, id);
    } catch (NoSuchMethodException e) {
      // no fromString(String) method
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RegenerationException("Could not call fromString(String) method", e);
    }
    
    // Look for single-argument String constructor
    try {
      Constructor constructor = cls.getConstructor(String.class);
      return (I) constructor.newInstance(id);
    } catch (NoSuchMethodException e) {
      throw new RegenerationException("Unable to convert id '" + id + "' to type '" + cls.getCanonicalName() + "'.");
    } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
      throw new RegenerationException("Could not call single-argument String constructor", e);
    }
  }
  
}
