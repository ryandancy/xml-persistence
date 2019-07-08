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
  
  /**
   * Create a {@link NullElement} from a given XML element. The {@link NullElement} will have the same tag name as the
   * XML element.
   * @param element An XML element in a valid format. It must have no children, no text, and exactly one attribute:
   *  null="true".
   * @return A {@link TextElement} representing the XML element.
   * @throws RegenerationException If the XML element has children, text, or bad attributes.
   */
  public static NullElement fromXmlElement(Element element) throws RegenerationException {
    if (element.hasChildNodes()) {
      throw new RegenerationException("Tried to get a NullElement from its XML element, but it has children!");
    }
    if (element.getTextContent() != null && !element.getTextContent().isEmpty()) {
      throw new RegenerationException("Tried to get a NullElement from its XML element, but it has text!");
    }
    if (!element.hasAttribute("null")) {
      throw new RegenerationException(
          "Tried to get a NullElement from its XML element, but there is no `null` attribute!");
    }
    if (element.getAttributes().getLength() > 1) {
      throw new RegenerationException(
          "Tried to get a NullElement from its XML element, but it has too many attributes!");
    }
    return new NullElement(element.getTagName());
  }
  
}
