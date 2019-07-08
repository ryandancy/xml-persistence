package ca.keal.persistence;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.w3c.dom.Document;

/**
 * Encapsulates all state that is global to a single call to {@link XmlPersistor#fromXml(Document)}. Currently, this
 * includes the {@link ToplevelList}, the {@link RegenToplevelRegistry}, and an {@link Objenesis} instance.
 * @see PersistingState
 */
class RegenState {
  
  private final ToplevelList toplevelList = new ToplevelList();
  private final RegenToplevelRegistry toplevelRegistry = new RegenToplevelRegistry();
  
  // stored here to improve performance using objenesis' cache
  private final Objenesis objenesis = new ObjenesisStd();
  
  public ToplevelList getToplevelList() {
    return toplevelList;
  }
  
  public RegenToplevelRegistry getToplevelRegistry() {
    return toplevelRegistry;
  }
  
  public Objenesis getObjenesis() {
    return objenesis;
  }
  
}
