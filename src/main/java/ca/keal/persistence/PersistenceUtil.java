package ca.keal.persistence;

import java.util.regex.Pattern;

/**
 * Contains utility methods for use by the persistence logic.
 */
final class PersistenceUtil {
  
  /** Matches valid XML tags - https://stackoverflow.com/a/5396246 */
  private static final Pattern XML_TAG_REGEX = Pattern.compile("^[:A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff" 
      + "\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff\\uf900-\\ufdcf" 
      + "\\ufdf0-\\ufffd][:A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-" 
      + "\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\udfff\\uf900-\\ufdcf\\ufdf0-\\ufffd" 
      + "\\-.0-9\\u00b7\\u0300-\\u036f\\u203f-\\u2040]*\\Z");
  
  /** This class cannot be instantiated */
  private PersistenceUtil() {}
  
  /**
   * Verify that {@code cls} is annotated with @{@link Persistable} properly, then return the annotation.
   * @param cls The class whose @{@link Persistable} annotation will be verified and returned.
   * @return The @{@link Persistable} annotation of {@code cls}.
   * @throws PersistenceException If the class is not annotated with @{@link Persistable} or if its
   *  {@link Persistable#toplevel} parameter is {@code true} and any of the following conditions is not met:
   *  <ul>
   *    <li>{@code tag} is not supplied</li>
   *    <li>{@code tag} is not a valid XML tag, as determined by {@link #isValidXmlTag(String)}</li>
   *    <li>{@code idField} is not supplied</li>
   *    <li>{@code idField} is not a valid instance-level field in {@code cls}</li>
   *  </ul>
   */
  static Persistable verifyAndGetPersistable(Class<?> cls) {
    Persistable persistable = cls.getAnnotation(Persistable.class);
    
    // Verify that it's @Persistable
    if (persistable == null) {
      throw new PersistenceException(cls.getCanonicalName() + " must be annotated @Persistable");
    }
    
    // Verify toplevel restrictions
    if (persistable.toplevel()) {
      // Verify that the tag is a valid XML tag name - THIS DOES NOT verify that it's unique
      String tag = persistable.tag();
      if (tag.isEmpty()) {
        throw new PersistenceException(cls.getCanonicalName()
            + " is @Persistable with toplevel=true and so must supply the tag parameter");
      }
      if (!isValidXmlTag(tag)) {
        throw new PersistenceException(cls.getCanonicalName()
            + "'s @Persistable tag parameter must be a valid XML tag name");
      }
      
      // Verify that the idField is a field name that exists in the class
      String idField = persistable.idField();
      if (idField.isEmpty()) {
        throw new PersistenceException(cls.getCanonicalName()
            + " is @Persistable with toplevel=true and so must supply the idField parameter");
      }
      try {
        cls.getDeclaredField(idField);
      } catch (NoSuchFieldException e) {
        throw new PersistenceException(cls.getCanonicalName()
            + "'s @Persistable idField parameter must be the name of a present instance-level field");
      }
    }
    
    return persistable;
  }
  
  /**
   * Return whether {@code tag} is a valid XML tag.
   */
  static boolean isValidXmlTag(String tag) {
    return tag != null && !tag.isEmpty() && !tag.startsWith("xml") && XML_TAG_REGEX.matcher(tag).find();
  }
  
  /**
   * Pick an appropriate {@link PersistRegenStrategy} to persist the given class and object and return it.
   */
  static <R> PersistRegenStrategy<R> pickStrategy(Class<R> cls, R object) {
    if (object == null) {
      return new NullPRStrategy<>(cls);
    }
    return pickStrategy(cls);
  }
  
  /**
   * Pick an appropriate {@link PersistRegenStrategy} to persist the given class and return it. This method will never
   * return {@link NullPRStrategy}.
   */
  static <R> PersistRegenStrategy<R> pickStrategy(Class<R> cls) {
    if (cls.isPrimitive() || cls.equals(String.class)) {
      return new PrimitivePRStrategy<>(cls);
    } else {
      return new PersistablePRStrategy<>(cls);
    }
  }
  
}