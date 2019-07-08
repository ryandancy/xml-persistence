package ca.keal.persistence;

/**
 * A {@link PersistRegenStrategy} which simply saves the value as a {@link TextElement}'s value using {@code toString()}
 * on the primitive passed in. This should be used for primitive and string fields.
 * @param <T> The type of the primitive that will be persisted with this strategy.
 */
// TODO do we even need to pass cls in here? It's not used
class PrimitivePRStrategy<T> extends PersistRegenStrategy<T> {
  
  /** Create a new {@link PrimitivePRStrategy} persisting and regenerating the specified class. */
  public PrimitivePRStrategy(Class<T> cls) {
    super(cls);
  }
  
  /**
   * @return A simple {@link TextElement} with the tag of the @{@link Persist} annotation's {@code value} and the text
   *  being {@code toPersist.toString()}.
   * @see PersistRegenStrategy#persist(PersistingState, Persist, Object)
   */
  @Override
  public PersistedElement persist(PersistingState state, Persist persistAnno, T toPersist) {
    return new TextElement(persistAnno.value(), toPersist.toString());
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public T regenerate(RegenState state, PersistedElement toRegen) throws RegenerationException {
    if (!(toRegen instanceof TextElement)) {
      throw new RegenerationException("Need a TextElement to regenerate a primitive");
    }
    
    String text = ((TextElement) toRegen).getText();
    Class<T> cls = getPersistingClass();
    
    // Regenerate according to the exact type of primitive; sadly we're gonna have to box to deal with generics
    // However, we can ignore all the unchecked warnings because we check the class beforehand
    try {
      if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
        return (T) Boolean.valueOf(Boolean.parseBoolean(text));
      } else if (cls.equals(byte.class) || cls.equals(Byte.class)) {
        return (T) new Byte(Byte.parseByte(text));
      } else if (cls.equals(char.class) || cls.equals(Character.class)) {
        if (text.length() != 1) {
          throw new RegenerationException("Trying to regenerate char with more than one character, or none");
        }
        return (T) new Character(text.charAt(0));
      } else if (cls.equals(short.class) || cls.equals(Short.class)) {
        return (T) new Short(Short.parseShort(text));
      } else if (cls.equals(int.class) || cls.equals(Integer.class)) {
        return (T) new Integer(Integer.parseInt(text));
      } else if (cls.equals(long.class) || cls.equals(Long.class)) {
        return (T) new Long(Long.parseLong(text));
      } else if (cls.equals(float.class) || cls.equals(Float.class)) {
        return (T) new Float(Float.parseFloat(text));
      } else if (cls.equals(double.class) || cls.equals(Double.class)) {
        return (T) new Double(Double.parseDouble(text));
      } else if (cls.equals(String.class)) {
        return (T) text;
      } else {
        throw new IllegalStateException("PrimitivePRStrategy has non-primitive class: " + cls.getCanonicalName());
      }
    } catch (NumberFormatException e) {
      throw new RegenerationException("Bad number format: '" + text + "' for " + cls.getCanonicalName(), e);
    }
  }
  
}
