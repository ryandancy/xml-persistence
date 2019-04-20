package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link PersistedElement} which is a parent element of other {@link PersistedElement}s. This will be used to store
 * persistable objects.
 */
class ParentElement extends PersistedElement {
  
  /** The list of children of this element. */
  private final List<PersistedElement> children = new ArrayList<>();
  
  /**
   * Create a new {@link ParentElement} with the specified tag name.
   * @param tag The name of the tag of this XML element. This must be a valid XML tag name as determined by
   *  {@link PersistenceUtil#isValidXmlTag(String)}.
   * @throws PersistenceException If {@code tag} is not a valid XML tag name.
   */
  public ParentElement(String tag) {
    super(tag);
  }
  
  /**
   * Add a child {@link PersistedElement} to this {@link ParentElement}. The child will then appear inside this
   * element when converted to XML.
   * @param child The child {@link PersistedElement} to add.
   * @throws NullPointerException If {@code child} is {@code null}.
   */
  public void addChild(PersistedElement child) {
    if (child == null) {
      throw new NullPointerException("Cannot add null child element");
    }
    children.add(child);
  }
  
  /**
   * @return An unmodifiable list of the children that have previously been added.
   */
  public List<PersistedElement> getChildren() {
    return Collections.unmodifiableList(children);
  }
  
  /**
   * Remove a child from this {@link ParentElement}. The child will no longer appear inside this element.
   * @param child The child {@link PersistedElement} to remove.
   * @return {@code true} if {@code child} existed in this {@link ParentElement}; in other words, if the child was
   * removed.
   */
  public boolean removeChild(PersistedElement child) {
    return children.remove(child);
  }
  
  /**
   * Create an XML element from this {@link ParentElement}. The created {@link Element} has this element's tag name and
   * contains the XML representations of all child {@link PersistedElement}s.
   * @param doc The {@link Document} with which to create the element.
   * @return An XML element representing this {@link ParentElement} containing all child elements' XML representations.
   */
  @Override
  public Element toXmlElement(Document doc) {
    Element element = super.toXmlElement(doc);
    for (PersistedElement child : children) {
      element.appendChild(child.toXmlElement(doc));
    }
    return element;
  }
}
