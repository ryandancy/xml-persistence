package ca.keal.persistence;

import org.w3c.dom.Document;

/**
 * Encapsulates all state that is global to a single call to {@link XmlPersistor#fromXml(Document)}. Currently, this
 * includes the {@link ToplevelList} and the {@link RegenToplevelRegistry}.
 * @see PersistingState
 */
class RegenState {
  
  private final ToplevelList toplevelList = new ToplevelList();
  private final RegenToplevelRegistry toplevelRegistry = new RegenToplevelRegistry();
  
  public ToplevelList getToplevelList() {
    return toplevelList;
  }
  
  public RegenToplevelRegistry getToplevelRegistry() {
    return toplevelRegistry;
  }
  
}
