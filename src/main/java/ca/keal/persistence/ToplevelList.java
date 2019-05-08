package ca.keal.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The toplevel list of {@link ToplevelElement}s. Global to each call of {@link XmlPersistor#toXml}. This is simply a
 * list of toplevel elements (really a map) indexed by tag name and ID.
 */
class ToplevelList {
  
  /** Simple POJO used as an index into {@link #idsToElements}. Encapsulates name + id. Neither may be null. */
  private static class ItemID {
    
    private final String name;
    private final String id;
    
    private ItemID(String name, String id) {
      if (name == null || id == null) {
        throw new NullPointerException("No null elements are allowed in the toplevel list");
      }
      this.name = name;
      this.id = id;
    }
  
    @Override
    public int hashCode() {
      return 37 * name.hashCode() * id.hashCode();
    }
  
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ItemID)) return false;
      ItemID itemId = (ItemID) obj;
      return name.equals(itemId.name) && id.equals(itemId.id);
    }
    
  }
  
  /**
   * The map backing this {@link ToplevelList}. Maps {@link ItemID}s to their corresponding {@link ToplevelElement}s
   * in the "list'.
   */
  private final Map<ItemID, ToplevelElement> idsToElements = new HashMap<>();
  
  /**
   * Does this {@link ToplevelList} contain an element with this tag name and ID?
   * @param tagName The name of the tag of the element being sought.
   * @param id The ID of the element being sought.
   * @return Whether a {@link ToplevelElement} with this tag name and ID is in the {@link ToplevelList}.
   * @throws NullPointerException If either parameter is {@code null}.
   */
  public boolean contains(String tagName, String id) {
    return idsToElements.containsKey(new ItemID(tagName, id));
  }
  
  /**
   * Add the element to this {@link ToplevelList}. This will overwrite any previous toplevel elements added with the
   * same tag name and ID, but this is bad practice and a warning will be printed if that occurs.
   * @param element The {@link ToplevelElement} to be added to the toplevel list.
   * @throws NullPointerException If the element is {@code null}.
   */
  public void addElement(ToplevelElement element) {
    if (element == null) {
      throw new NullPointerException("No null elements are allowed in the toplevel list");
    }
    
    ItemID itemId = new ItemID(element.getTag(), element.getId());
    if (idsToElements.containsKey(itemId)) {
      // TODO a real logging system
      System.err.print("Warning: added duplicate item with tag name '" + element.getTag() + "' and id '"
        + element.getId() + "' to toplevel list's map; this probably means there's a duplicate ID");
    }
    
    idsToElements.put(itemId, element);
  }
  
  /**
   * Return the toplevel element in the list with the specified name and ID, or {@code null} if there is no toplevel
   * element with the given name and ID in the list.
   */
  public ToplevelElement getElement(String name, String id) {
    return idsToElements.get(new ItemID(name, id));
  }
  
  /**
   * @return A collection of the {@link ToplevelElement}s in this {@link ToplevelList}.
   */
  public Collection<ToplevelElement> getAsCollection() {
    return idsToElements.values();
  }
  
}
