package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link ParentElement} which represents specifically a toplevel element. This essentially contains two specific
 * attributes: {@code root}, which is {@code true} if this element is the root element and not present otherwise, and
 * {@code id}, which is the ID that may be referenced by other elements to refer to this element.
 */
class ToplevelElement extends ParentElement {
  
  /** Whether this element is the root element. */
  private final boolean root;
  
  /** The ID of this element, by which other elements may refer to this element. */
  private final String id;
  
  /**
   * Create a new {@link ToplevelElement}.
   * @param tag The name of the tag of this XML element. This must be a valid XML tag name as determined by
   *  {@link PersistenceUtil#isValidXmlTag(String)}.
   * @param id The ID of this {@link ToplevelElement} by which other elements may refer to it.
   * @param root Whether this element is the root element.
   * @throws PersistenceException If {@code tag} is not a valid XML tag name.
   * @throws NullPointerException If {@code id} is {@code null}.
   */
  public ToplevelElement(String tag, String id, boolean root) {
    super(tag);
    if (id == null) {
      throw new NullPointerException("A toplevel element cannot have a null ID");
    }
    this.root = root;
    this.id = id;
  }
  
  /**
   * Create a new {@link ToplevelElement}.
   * @param tag The name of the tag of this XML element. This must be a valid XML tag name as determined by
   *  {@link PersistenceUtil#isValidXmlTag(String)}.
   * @param id The ID of this {@link ToplevelElement} by which other elements may refer to it.
   * @throws PersistenceException If {@code tag} is not a valid XML tag name.
   * @throws NullPointerException If {@code id} is {@code null}.
   */
  public ToplevelElement(String tag, String id) {
    this(tag, id, false);
  }
  
  /**
   * @return Whether this element is the root element.
   */
  public boolean isRoot() {
    return root;
  }
  
  /**
   * @return The ID of this element by which other elements may refer to it.
   */
  public String getId() {
    return id;
  }
  
  /**
   * Create an XML element from this {@link ToplevelElement}. The created {@link Element} has the tag name of this
   * element. This element's children's XML representations are children of the created {@link Element}. The
   * created {@link Element} has the attribute {@code id}, containing this element's ID, and if this element is a root
   * element, it has the attribute {@code root="true"}.
   * @param doc The {@link Document} with which to create the element.
   * @return An XML element representing this {@link ToplevelElement}.
   */
  @Override
  public Element toXmlElement(Document doc) {
    Element element = super.toXmlElement(doc);
    element.setAttribute("id", getId());
    if (isRoot()) {
      element.setAttribute("root", "true");
    }
    return element;
  }
  
}
