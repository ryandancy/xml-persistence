package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
   * Get a list of all child {@link PersistedElement}s with the given tag.
   * @param tag The tag for which to search in child elements.
   * @return A list of the child {@link PersistedElement}s whose tag equals the given tag.
   * @throws NullPointerException If {@code tag} is {@code null}.
   */
  public List<PersistedElement> getChildrenByTag(String tag) {
    if (tag == null) {
      throw new NullPointerException("Cannot get children with null tag");
    }
    
    return children.stream()
        .filter(child -> tag.equals(child.getTag()))
        .collect(Collectors.toList());
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
  
  /**
   * Create a {@link ParentElement} from a given XML element. The {@link ParentElement} will have the same tag name
   * as the XML element, and each child element will be loaded in as well.
   * @param element An XML element in a valid format. It must have no attributes and must have children.
   * @return A {@link ParentElement} representing the XML element.
   * @throws RegenerationException If the XML element has attributes, no children, or one of its children has an issue.
   */
  public static ParentElement fromXmlElement(Element element) throws RegenerationException {
    if (element.hasAttributes()) {
      throw new RegenerationException("Tried to get a ParentElement from its XML element, but it has attributes!");
    }
    return fromXmlElement(element, ParentElement::new);
  }
  
  /**
   * Same as {@link #fromXmlElement(Element)}, but does not check attributes and can create subclasses of
   * {@link ParentElement} via a method reference. For use in {@link ToplevelElement}.
   */
  protected static <P extends ParentElement> P fromXmlElement(Element element, Function<String, P> constructor)
      throws RegenerationException {
    if (!PersistenceUtil.elementHasChildren(element)) {
      throw new RegenerationException("Tried to get a ParentElement from its XML element, but it has no children!");
    }
    // You know what, we don't care if there's text content, we'll just ignore it
    
    P parent = constructor.apply(element.getTagName());
    
    // Add all the children
    for (int i = 0; i < element.getChildNodes().getLength(); i++) {
      Node childNode = element.getChildNodes().item(i);
      if (childNode instanceof Element) {
        Element childElement = (Element) childNode;
        PersistedElement child = createChildElement(childElement);
        parent.addChild(child);
      }
    }
    
    return parent;
  }
  
  /** Get a {@link PersistedElement} for a given child element. */
  private static PersistedElement createChildElement(Element child) throws RegenerationException {
    if (child.hasAttribute("null")) {
      return NullElement.fromXmlElement(child);
    } else if (PersistenceUtil.elementHasChildren(child)) {
      return ParentElement.fromXmlElement(child);
    } else {
      return TextElement.fromXmlElement(child);
    } // no ToplevelElement call because a ToplevelElement can't be a child
  }
  
}
