package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link PersistedElement} which contains text. This will be used to store primitives and IDs of toplevel elements.
 */
class TextElement extends PersistedElement {
  
  /** The text contained in this element. */
  private final String text;
  
  /**
   * Create a new {@link TextElement}.
   * @param tag The name of the tag of this XML element. This must be a valid XML tag name as determined by
   *   {@link PersistenceUtil#isValidXmlTag(String)}.
   * @param text The text that this {@link TextElement} will contain.
   * @throws PersistenceException If {@code tag} is not a valid XML tag name.
   * @throws NullPointerException If {@code text} is {@code null}.
   */
  public TextElement(String tag, String text) {
    super(tag);
    if (text == null) {
      throw new NullPointerException("A TextElement cannot have null text");
    }
    this.text = text;
  }
  
  /**
   * @return The text contained in this element
   */
  public String getText() {
    return text;
  }
  
  /**
   * Create an XML element from this {@link TextElement}. The {@link Element} will have this {@link TextElement}'s tag
   * name and will contain its text.
   * @param doc The {@link Document} with which to create the element.
   * @return An XML element representing this {@link TextElement}.
   */
  @Override
  public Element toXmlElement(Document doc) {
    Element element = super.toXmlElement(doc);
    element.setTextContent(text);
    return element;
  }
  
  /**
   * Create a {@link TextElement} from a given XML element. The {@link TextElement} will have the same tag name
   * and text content as the XML element.
   * @param element An XML element in a valid format. It must have no children and no attributes.
   * @return A {@link TextElement} representing the XML element.
   * @throws RegenerationException If the XML element has children or attributes.
   */
  public static TextElement fromXmlElement(Element element) throws RegenerationException {
    if (element.hasChildNodes()) {
      throw new RegenerationException("Tried to get a TextElement from its XML element, but it has children!");
    }
    if (element.hasAttributes()) {
      throw new RegenerationException("Tried to get a TextElement from its XML element, but it has attributes! " 
          + element.getAttributes());
    }
    return new TextElement(element.getTagName(), element.getTextContent());
  }
  
}
