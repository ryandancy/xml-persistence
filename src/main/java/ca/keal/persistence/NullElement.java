package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link PersistedElement} which represents a null value.
 */
class NullElement extends PersistedElement {
  
  /**
   * Create a new {@link NullElement}.
   * 
   * @param tag The name of the tag of this XML element. This must be a valid XML tag name as determined by {@link
   * PersistenceUtil#isValidXmlTag(String)}.
   * @throws PersistenceException If {@code tag} is not a valid XML tag name.
   */
  public NullElement(String tag) {
    super(tag);
  }
  
  /**
   * Create an XML element from this {@link NullElement}. The element will be empty with the tag provided in the
   * constructor and the attribute {@code null="true"}.
   * @param doc The {@link Document} with which to create the element.
   * @return An XML element representing this {@link NullElement}.
   */
  @Override
  public Element toXmlElement(Document doc) {
    Element element = super.toXmlElement(doc);
    element.setAttribute("null", "true");
    return element;
  }
  
}
