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
  
  @Persistable(toplevel=true, tag="PrimitivesOnlyTest", idField="id")
  private static class PrimitivesOnlyTest {
    
    @SuppressWarnings("unused") private final String id = "thisIsMyId";
    
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
    
    Source test = Input.fromDocument(persisted).build();
    Source control = Input.fromFile("src/test/resources/primitives-only-test.xml").build();
    
    Diff diff = DiffBuilder.compare(control)
        .withTest(test)
        .ignoreComments()
        .ignoreWhitespace()
        .ignoreElementContentWhitespace()
        .build();
    
    assertFalse(diff.hasDifferences(), diff.toString());
  }
  
}
