package ca.keal.persistence;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;

class XmlPersistorTest {
  
  /** A utility for printing a given Document. */
  private void printDocument(Document doc) {
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
      
      transformer.transform(new DOMSource(doc),
          new StreamResult(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)));
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }
  
  private void assertSame(Document testDoc, String controlLocation) {
    Source test = Input.fromDocument(testDoc).build();
    Source control = Input.fromFile(controlLocation).build();
    
    Diff diff = DiffBuilder.compare(control)
        .withTest(test)
        .ignoreComments()
        .ignoreWhitespace()
        .ignoreElementContentWhitespace()
        .build();
    
    assertFalse(diff.hasDifferences(), diff.toString());
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="PrimitivesOnlyTest", idField="id")
  @SuppressWarnings("unused")
  private static class PrimitivesOnlyTest {
    
    private final String id = "thisIsMyId";
    
    @Persist("myPrivateInt") private int mpi = -242817;
    @Persist("myDefaultLong") long mdl = 147124869198241L;
    @Persist("myProtectedChar") protected char mpc = 'a';
    @Persist("myPublicByte") public byte mpb = -41;
    
    @Persist("myString") public String ms = "hello"; // Strings count as primitives too
    
    private String notPersisted = "spooky";
    
  }
  
  @Test
  void primitivesOnlyToXml() {
    XmlPersistor<PrimitivesOnlyTest> persistor = new XmlPersistor<>(PrimitivesOnlyTest.class);
    Document persisted = persistor.toXml(new PrimitivesOnlyTest());
    assertSame(persisted, "src/test/resources/primitives-only-test.xml");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="simpleEmbeddedTest", idField="id")
  @SuppressWarnings("unused")
  private static class SimpleEmbeddedTest {
    
    private final String id = "simpleEmbedded";
    
    @Persist("primitiveInt") private final int pint = 42;
    
    @Persist("embedded1") private SimpleEmbedded e1 = new SimpleEmbedded("abc", "def");
    @Persist("embedded2") private SimpleEmbedded e2 = new SimpleEmbedded("123", "456");
    
  }
  
  @Persistable
  @SuppressWarnings("unused")
  private static class SimpleEmbedded {
    
    @Persist("thing1") private final String thing1;
    @Persist("thing2") private final String thing2;
    
    SimpleEmbedded(String thing1, String thing2) {
      this.thing1 = thing1;
      this.thing2 = thing2;
    }
    
  }
  
  @Test
  void simpleEmbeddedToXml() {
    XmlPersistor<SimpleEmbeddedTest> persistor = new XmlPersistor<>(SimpleEmbeddedTest.class);
    Document persisted = persistor.toXml(new SimpleEmbeddedTest());
    assertSame(persisted, "src/test/resources/simple-embedded-test.xml");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="circularRoot", idField="id")
  @SuppressWarnings("unused")
  private static class DualCircularToplevelRoot {
    
    private final String id;
    
    @Persist("toplevelSide") private final DualCircularToplevelSide side;
    
    DualCircularToplevelRoot(String idMe, String idSide) {
      this.id = idMe;
      this.side = new DualCircularToplevelSide(idSide, this);
    }
    
  }
  
  @Persistable(toplevel=true, tag="circularSide", idField="id")
  @SuppressWarnings("unused")
  private static class DualCircularToplevelSide {
    
    private final String id;
    
    @Persist("toplevelRoot") private final DualCircularToplevelRoot root;
    
    DualCircularToplevelSide(String id, DualCircularToplevelRoot root) {
      this.id = id;
      this.root = root;
    }
    
  }
  
  @Test
  void dualCircularToplevelToXml() {
    XmlPersistor<DualCircularToplevelRoot> persistor = new XmlPersistor<>(DualCircularToplevelRoot.class);
    Document persisted = persistor.toXml(new DualCircularToplevelRoot("foo", "bar"));
    assertSame(persisted, "src/test/resources/dual-circular-toplevel-test.xml");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="singleCircle", idField="id")
  @SuppressWarnings("unused")
  private static class SingleCircularToplevelTest {
    
    private final String id = "foobar";
    
    @Persist("heyLookItsMe") private final SingleCircularToplevelTest me;
    
    @Persist("dogInFrenchIs") private final String dog = "chien";
    
    SingleCircularToplevelTest() {
      me = this;
    }
    
  }
  
  @Test
  void singleCircularToplevelToXml() {
    XmlPersistor<SingleCircularToplevelTest> persistor = new XmlPersistor<>(SingleCircularToplevelTest.class);
    Document persisted = persistor.toXml(new SingleCircularToplevelTest());
    assertSame(persisted, "src/test/resources/single-circular-toplevel-test.xml");
  }
  
}
