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
  
}
