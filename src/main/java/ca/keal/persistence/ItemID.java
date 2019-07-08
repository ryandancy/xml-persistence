package ca.keal.persistence;

/**
 * Simple POJO used as an index in {@link ToplevelList} and {@link RegenToplevelRegistry}. Encapsulates name + id.
 * Neither may be null.
 */
class ItemID {
  
  private final String name;
  private final String id;
  
  ItemID(String name, String id) {
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
  
  @Override
  public String toString() {
    return "ItemID[name=" + name + ", id=" + id + "]";
  }
  
}
