package ca.keal.persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Records a mapping between toplevel @Persistable classes and their tags and raises {@link PersistenceException} if
 * a duplicate tag is encountered.
 */
// This is probably overkill (it works fine I guess as long as there are no duplicate IDs) but semantically two classes
// shouldn't have the same tag, so whatever
class DuplicatePersistableChecker {
  
  private final Map<String, Class<?>> tagsToClasses = new HashMap<>();
  
  /**
   * Check that there is no other class than {@code cls} already registered with the given toplevel @Persistable tag. If
   * so, register the tag and class pair for future checking.
   * @param tag {@code cls}'s @Persistable tag to check.
   * @param cls The class marked @Persistable(topleve=true).
   */
  public void checkAndRegister(String tag, Class<?> cls) {
    if (tag == null || cls == null) {
      throw new NullPointerException("DuplicatePersistableChecker does not support null tags or classes.");
    }
    if (tagsToClasses.containsKey(tag) && !cls.equals(tagsToClasses.get(tag))) {
      throw new PersistenceException("Duplicate @Persistable tag: '" + tag + "' encountered both on '"
        + tagsToClasses.get(tag).getCanonicalName() + "' and '" + cls.getCanonicalName() + "'.");
    }
    tagsToClasses.put(tag, cls);
  }
  
}
