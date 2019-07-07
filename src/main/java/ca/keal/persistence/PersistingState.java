package ca.keal.persistence;

/**
 * Encapsulates all state that is global to a single call to {@link XmlPersistor#toXml(Object)}. Currently, this
 * includes the {@link ToplevelList} and the {@link DuplicatePersistableChecker}.
 */
class PersistingState {
  
  private final ToplevelList toplevelList = new ToplevelList();
  private final DuplicatePersistableChecker duplicateChecker = new DuplicatePersistableChecker();
  
  public ToplevelList getToplevelList() {
    return toplevelList;
  }
  
  public DuplicatePersistableChecker getDuplicateChecker() {
    return duplicateChecker;
  }
  
}
