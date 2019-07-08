package ca.keal.persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple shell around a map for registering regenerated objects corresponding to {@link ToplevelElement}s with
 * given names and IDs. Global to each call of {@link XmlPersistor#fromXml}.
 */
class RegenToplevelRegistry {
  
  private final Map<ItemID, Object> idsToObjs = new HashMap<>();
  
  /**
   * @return Whether the registry contains an object with the specified name and ID.
   */
  boolean contains(String name, String id) {
    return idsToObjs.containsKey(new ItemID(name, id));
  }
  
  /**
   * Registers {@code obj} under the given name and ID. Will emit a warning if the name and ID are a duplicate.
   * @throws NullPointerException If any parameter is null.
   */
  <T> void register(String name, String id, T obj) {
    if (obj == null) {
      throw new NullPointerException("Cannot register a null object");
    }
    ItemID itemID = new ItemID(name, id);
    if (idsToObjs.containsKey(itemID)) {
      System.err.println("Warning: registering duplicate object under itemID: " + itemID);
    }
    idsToObjs.put(itemID, obj);
  }
  
  /**
   * @return The object registered under the given name and ID.
   * @param <T> The type of the object.
   * @throws ClassCastException If the object is not of the parameterized type.
   */
  @SuppressWarnings("unchecked")
  <T> T get(String name, String id) {
    return (T) idsToObjs.get(new ItemID(name, id));
  }
  
}
