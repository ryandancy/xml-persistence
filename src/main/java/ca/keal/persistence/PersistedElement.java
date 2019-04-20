package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An XML element representing a persisted value. This is the superclass of the persistence-specific abstraction of
 * the org.w3c.dom library. This superclass has a tag name, which must be a valid XML tag name, and can create an
 * XML element ({@link org.w3c.dom.Element}) with that tag name.
 */
class PersistedElement {
  
  /** This element's tag name. */
  private final String tag;
  
  /**
   * Create a new {@link PersistedElement} with the specified tag name.
   * @param tag The name of the tag of this XML element. This must be a valid XML tag name as determined by
   *  {@link PersistenceUtil#isValidXmlTag(String)}.
   * @throws PersistenceException If {@code tag} is not a valid XML tag name.
   */
  public PersistedElement(String tag) {
    if (!PersistenceUtil.isValidXmlTag(tag)) {
      throw new PersistenceException("'" + tag + "' is an invalid tag name");
    }
    this.tag = tag;
  }
  
  /**
   * @return This element's tag name.
   */
  public String getTag() {
    return tag;
  }
  
  /**
   * Create an XML element from this {@link PersistedElement}. The created {@link Element} has the tag name of this
   * {@link PersistedElement}.
   * @param doc The {@link Document} with which to create the element.
   * @return An XML element representing this {@link PersistedElement}.
   */
  public Element toXmlElement(Document doc) {
    return doc.createElement(getTag());
  }
  
}
