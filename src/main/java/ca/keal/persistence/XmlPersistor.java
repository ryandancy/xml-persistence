package ca.keal.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.annotation.Annotation;

/**
 * Handles persisting an @{@link Persistable} object of type {@code R} to XML and regenerating it from XML.
 * @param <R> The type of object that may be persisted or regenerated by this {@code XmlPersistor}. This must
 *  be @{@link Persistable} with {@code toplevel=true}.
 */
// TODO add arrays and lists as "primitive" types / maybe with a different PersistStrategy
// TODO should we *really* be using objenesis? like should we *really*? or should we require default constructors?
public class XmlPersistor<R> {
  
  private static final String ROOT_ELEMENT_NAME = "persisted";
  
  // A base instance of Persist used for the root element
  private static final Persist ROOT_PERSIST_ANNOTATION = new Persist() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Persist.class;
    }
    
    @Override
    public String value() {
      return "root-persist-tag-YOU-SHOULD-NOT-SEE-THIS";
    }
  };
  
  private final Class<R> rootClass;
  private final Persistable rootAnnotation;
  
  /**
   * Instantiate an {@link XmlPersistor}.
   * @param rootClass The class of the object to be persisted or regenerated. This must be @{@link Persistable} with
   *  {@code toplevel=true}.
   * @throws NullPointerException If {@code rootClass} is {@code null}.
   * @throws PersistenceException If {@code rootClass} does not meet the above criteria.
   */
  public XmlPersistor(Class<R> rootClass) {
    if (rootClass == null) {
      throw new NullPointerException("rootClass cannot be null");
    }
    
    rootAnnotation = PersistenceUtil.verifyAndGetPersistable(rootClass);
    if (!rootAnnotation.toplevel()) {
      throw new PersistenceException(rootClass.getCanonicalName()
          + " is the root class and as such must have toplevel=true in @Persistable");
    }
    
    this.rootClass = rootClass;
  }
  
  /**
   * Persist {@code root} to an XML document.
   * @param root The object to be persisted.
   * @return The XML document representing the persisted version of {@code root}.
   * @throws NullPointerException If {@code root} is {@code null}.
   * @throws PersistenceException If an error is encountered when persisting {@code root}.
   */
  public Document toXml(R root) {
    if (root == null) {
      // TODO maybe support persisting null objects if a use case exists
      throw new NullPointerException("Cannot persist null objects");
    }
    
    // Persist the root element first
    PersistingState state = new PersistingState();
    PersistRegenStrategy<R> strategy = PersistenceUtil.pickStrategy(rootClass, root);
    
    // We can do this because we checked that it's toplevel in the constructor
    TextElement idElement = (TextElement) strategy.persist(state, ROOT_PERSIST_ANNOTATION, root);
    
    // Find the toplevel element with this ID and set it to root
    state.getToplevelList().getElement(rootAnnotation.tag(), idElement.getText()).setRoot(true);
    
    // Load it all into an XML document and return
    Document doc;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      // Why must you do this Java, this really should never happen
      throw new IllegalStateException("Something has, apparently, gone very wrong.", e);
    }
  
    Element rootElement = doc.createElement(ROOT_ELEMENT_NAME);
    doc.appendChild(rootElement);
    
    for (ToplevelElement element : state.getToplevelList().getAsCollection()) {
      rootElement.appendChild(element.toXmlElement(doc));
    }
    
    return doc;
  }
  
  /**
   * Regenerate an object from {@code doc}.
   * @param doc The XML document from which to regenerate the object.
   * @return The object regenerated from {@code doc}.
   * @throws NullPointerException If {@code doc} is {@code null}.
   * @throws RegenerationException If an error is encountered when regenerating the object.
   */
  public R fromXml(Document doc) throws RegenerationException {
    if (doc == null) {
      throw new NullPointerException("Cannot regenerate from a null Document");
    }
    
    Element docRoot = doc.getDocumentElement();
    if (!docRoot.getTagName().equals("persisted")) {
      System.err.println("WARNING: root tag name is '" + docRoot.getTagName() + "', not 'persisted'.");
    }
    if (docRoot.hasAttributes()) {
      System.err.println("WARNING: root tag has attributes for some reason.");
    }
    
    try {
      // Load everything into the ToplevelList + find the root ToplevelElement
      RegenState state = new RegenState();
      ToplevelElement root = null;
      
      for (int i = 0; i < docRoot.getChildNodes().getLength(); i++) { // for some reason NodeList isn't Iterable
        Node childNode = docRoot.getChildNodes().item(i);
        if (childNode instanceof Element) {
          Element child = (Element) childNode;
          ToplevelElement childToplevel = ToplevelElement.fromXmlElement(child);
          state.getToplevelList().addElement(childToplevel);
          
          if (childToplevel.isRoot()) {
            if (root != null) {
              throw new RegenerationException("Multiple toplevel nodes marked `root`");
            }
            root = childToplevel;
          }
        }
      }
      
      if (root == null) {
        throw new RegenerationException("No root toplevel node");
      }
      
      // Regenerate from the root
      PersistRegenStrategy<R> strategy = PersistenceUtil.pickStrategy(rootClass, root);
      R regenerated = strategy.regenerate(state, root);
      
      // Warn if any toplevel isn't used
      for (ItemID itemID : state.getToplevelList().getItemIDs()) {
        if (!state.getToplevelRegistry().contains(itemID.getName(), itemID.getId())) {
          System.err.println("Warning: unused toplevel element with tag name '" + itemID.getName()
              + "' and id '");
        }
      }
      
      return regenerated;
    } catch (PersistenceException e) {
      // some common persist/regen methods throw PersistenceExceptions, so we just rethrow as RegenerationExceptions
      throw new RegenerationException(e.getMessage(), e.getCause());
    }
  }
  
}
