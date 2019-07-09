package ca.keal.persistence;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO - test with @Persist Integers/other boxed types (null?)
// TODO test failure with empty string/null ID
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
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      
      transformer.transform(new DOMSource(doc),
          new StreamResult(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)));
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }
  
  private Document load(String filePath) throws Exception {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    return builder.parse(new File(filePath));
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
  // POSITIVE TESTS - toXml()
  
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
    
    private SimpleEmbedded(String thing1, String thing2) {
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
    
    private DualCircularToplevelRoot(String idMe, String idSide) {
      this.id = idMe;
      this.side = new DualCircularToplevelSide(idSide, this);
    }
  }
  
  @Persistable(toplevel=true, tag="circularSide", idField="id")
  @SuppressWarnings("unused")
  private static class DualCircularToplevelSide {
    private final String id;
    
    @Persist("toplevelRoot") private final DualCircularToplevelRoot root;
    
    private DualCircularToplevelSide(String id, DualCircularToplevelRoot root) {
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
    
    private SingleCircularToplevelTest() {
      me = this;
    }
  }
  
  @Test
  void singleCircularToplevelToXml() {
    XmlPersistor<SingleCircularToplevelTest> persistor = new XmlPersistor<>(SingleCircularToplevelTest.class);
    Document persisted = persistor.toXml(new SingleCircularToplevelTest());
    assertSame(persisted, "src/test/resources/single-circular-toplevel-test.xml");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="nullThings", idField="id")
  @SuppressWarnings("unused")
  private static class NullThingsTest {
    private final String id = "foo";
    @Persist("nullNonToplevel") private final NullThing thing1 = null;
    @Persist("nonNullNonToplevel") private final NullThing thing2 = new NullThing();
    @Persist("nullToplevel") private final ToplevelNullThing tlThing1 = null;
    @Persist("nonNullToplevel") private final ToplevelNullThing tlThing2 = new ToplevelNullThing();
  }
  
  @Persistable
  @SuppressWarnings("unused")
  private static class NullThing {
    @Persist("thisIsNullString") private final String nullThing = null;
    @Persist("thisHasValueNull") private final String notNull = "null";
  }
  
  @Persistable(toplevel=true, tag="somethingElseNull", idField="myId")
  @SuppressWarnings("unused")
  private static class ToplevelNullThing {
    private final int myId = 20141;
    @Persist("someInteger") private final int whatever = -1;
    @Persist("somethingNull") private final String imNull = null;
  }
  
  @Test
  void nullThingsToXml() {
    XmlPersistor<NullThingsTest> persistor = new XmlPersistor<>(NullThingsTest.class);
    Document persisted = persistor.toXml(new NullThingsTest());
    assertSame(persisted, "src/test/resources/null-things-test.xml");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="noPersistFields", idField="id")
  @SuppressWarnings("unused")
  private static class SimpleNoPersistFieldsTest {
    private final String id = "hello";
    private int dontPersist;
  }
  
  @Test
  void simpleNoPersistFieldsToXml() {
    XmlPersistor<SimpleNoPersistFieldsTest> persistor = new XmlPersistor<>(SimpleNoPersistFieldsTest.class);
    Document persisted = persistor.toXml(new SimpleNoPersistFieldsTest());
    assertSame(persisted, "src/test/resources/simple-no-persist-fields-test.xml");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="embeddedNoPersistFields", idField="schmoe")
  @SuppressWarnings("unused")
  private static class EmbeddedNoPersistFieldsTest {
    private String schmoe = "joe";
    @Persist("embedded1") private EmbeddedNoPersistFields e1;
    @Persist("embedded2") private EmbeddedNoPersistFields e2;
  }
  
  @Persistable
  @SuppressWarnings("unused")
  private static class EmbeddedNoPersistFields {}
  
  @Test
  void embeddedNoPersistFieldsToXml() {
    XmlPersistor<EmbeddedNoPersistFieldsTest> persistor = new XmlPersistor<>(EmbeddedNoPersistFieldsTest.class);
    EmbeddedNoPersistFieldsTest test = new EmbeddedNoPersistFieldsTest();
    test.e1 = new EmbeddedNoPersistFields();
    test.e2 = new EmbeddedNoPersistFields();
    Document persisted = persistor.toXml(test);
    assertSame(persisted, "src/test/resources/embedded-no-persist-fields-test.xml");
  }
  
  // ==========================================================================================
  // NEGATIVE TESTS - toXml()
  // @Persistable attribute validity was tested in PersistenceUtilTest so whatever on that
  
  private static class NotPersistable {}
  
  @Persistable(toplevel=true, tag="invalid", idField="id")
  @SuppressWarnings("unused")
  private static class PersistingNotPersistable {
    private final String id = "hello";
    @Persist("notPersistable") private NotPersistable foo = new NotPersistable();
  }
  
  @Test
  void persistingNonPersistableClassThrows() {
    XmlPersistor<PersistingNotPersistable> persistor = new XmlPersistor<>(PersistingNotPersistable.class);
    PersistenceException e = assertThrows(PersistenceException.class,
        () -> persistor.toXml(new PersistingNotPersistable()));
    assertTrue(e.getMessage().contains("must be annotated @Persistable"));
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="invalid", idField="id")
  @SuppressWarnings("unused")
  private static class EmptyStringTagOnPersist {
    private final String id = "foobar";
    @Persist("") private int hi = 2;
  }
  
  @Test
  void emptyStringAsTagThrows() {
    XmlPersistor<EmptyStringTagOnPersist> persistor = new XmlPersistor<>(EmptyStringTagOnPersist.class);
    PersistenceException e = assertThrows(PersistenceException.class,
        () -> persistor.toXml(new EmptyStringTagOnPersist()));
    assertTrue(e.getMessage().contains("invalid tag name"));
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="invalid", idField="id")
  @SuppressWarnings("unused")
  private static class TwoFieldsWithSameTag {
    private final String id = "foobar";
    
    @Persist("common") private int foo = 1234;
    @Persist("common") private String baz = "quux";
  }
  
  @Test
  void twoFieldsWithSameTagThrows() {
    XmlPersistor<TwoFieldsWithSameTag> persistor = new XmlPersistor<>(TwoFieldsWithSameTag.class);
    PersistenceException e = assertThrows(PersistenceException.class,
        () -> persistor.toXml(new TwoFieldsWithSameTag()));
    assertTrue(e.getMessage().toLowerCase().contains("duplicate"));
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="commonTag", idField="id")
  @SuppressWarnings("unused")
  private static class FirstToplevelWithSameTag {
    private final String id = "foobar";
    @Persist("youAreMyTwin") private SecondToplevelWithSameTag second = new SecondToplevelWithSameTag();
  }
  
  
  @Persistable(toplevel=true, tag="commonTag", idField="id")
  @SuppressWarnings("unused")
  private static class SecondToplevelWithSameTag {
    private final String id = "bazquux";
  }
  
  @Test
  void twoToplevelsWithSameTagThrows() {
    XmlPersistor<FirstToplevelWithSameTag> persistor = new XmlPersistor<>(FirstToplevelWithSameTag.class);
    PersistenceException e = assertThrows(PersistenceException.class,
        () -> persistor.toXml(new FirstToplevelWithSameTag()));
    assertTrue(e.getMessage().toLowerCase().contains("duplicate"));
  }
  
  // ==========================================================================================
  
  @Persistable
  private static class NonToplevelRoot {}
  
  @Test
  void persistingNonToplevelThrows() {
    PersistenceException e = assertThrows(PersistenceException.class, () -> new XmlPersistor<>(NonToplevelRoot.class));
    assertTrue(e.getMessage().toLowerCase().contains("toplevel"));
  }
  
  private static class NotEvenPersistable {}
  
  @Test
  void persistingNonPersistableThrows() {
    PersistenceException e = assertThrows(PersistenceException.class,
        () -> new XmlPersistor<>(NotEvenPersistable.class));
    assertTrue(e.getMessage().toLowerCase().contains("persistable"));
  }
  
  @Test
  void persistingNullClassThrows() {
    assertThrows(NullPointerException.class, () -> new XmlPersistor<>(null));
  }
  
  @Test
  void persistingNullThrows() {
    XmlPersistor<PrimitivesOnlyTest> persistor = new XmlPersistor<>(PrimitivesOnlyTest.class);
    assertThrows(NullPointerException.class, () -> persistor.toXml(null));
  }
  
  // ==========================================================================================
  // POSITIVE TESTS - fromXml()
  
  @Persistable(toplevel=true, tag="hello", idField="id")
  @SuppressWarnings("unused")
  private static class PrimitivesOnlyRegenTest {
    private final int id;
    @Persist("boolean") private final boolean aBoolean;
    @Persist("byte") private final byte aByte;
    @Persist("short") private final short aShort;
    @Persist("int") private final int anInt;
    @Persist("long") private final long aLong;
    @Persist("float") private final float aFloat;
    @Persist("double") private final double aDouble;
    @Persist("char") private final char aChar;
    @Persist("string") private final String aString;
    private PrimitivesOnlyRegenTest(int id, boolean a, byte b, short c, int d,
                                    long e, float f, double g, char h, String i) {
      this.id = id; aBoolean = a; aByte = b; aShort = c; anInt = d;
      aLong = e; aFloat = f; aDouble = g; aChar = h; aString = i;
    }
  }
  
  @Test
  void primitivesOnlyFromXml() throws Exception {
    XmlPersistor<PrimitivesOnlyRegenTest> persistor = new XmlPersistor<>(PrimitivesOnlyRegenTest.class);
    PrimitivesOnlyRegenTest control = new PrimitivesOnlyRegenTest(-1234, true, (byte) -24, (short) 634, -412,
        29586112412421L, -41.6235f, 2014.1241, 'X', "Hello from the other side");
    assertThat(persistor.fromXml(load("src/test/resources/primitives-only-regen-test.xml")))
        .isEqualToComparingFieldByField(control);
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="simpleEmbeddedTest", idField="id")
  @SuppressWarnings("unused")
  private static class SimpleEmbeddedRegenTest {
    private final String id = "simpleEmbedded";
    
    @Persist("primitiveInt") private final int pint;
    
    @Persist("embedded1") private SimpleEmbedded e1;
    @Persist("embedded2") private SimpleEmbedded e2;
    
    private SimpleEmbeddedRegenTest(int pint, SimpleEmbedded e1, SimpleEmbedded e2) {
      this.pint = pint;
      this.e1 = e1;
      this.e2 = e2;
    }
  }
  
  @Test
  void simpleEmbeddedFromXml() throws Exception {
    XmlPersistor<SimpleEmbeddedRegenTest> persistor = new XmlPersistor<>(SimpleEmbeddedRegenTest.class);
    SimpleEmbeddedRegenTest control = new SimpleEmbeddedRegenTest(1234,
        new SimpleEmbedded("aaa", "bbb"), new SimpleEmbedded("ccc", "ddd"));
    assertThat(persistor.fromXml(load("src/test/resources/simple-embedded-regen-test.xml")))
        .isEqualToComparingFieldByFieldRecursively(control);
  }
  
  // ==========================================================================================
  
  @Test
  void dualCircularToplevelFromXml() throws Exception {
    XmlPersistor<DualCircularToplevelRoot> persistor = new XmlPersistor<>(DualCircularToplevelRoot.class);
    DualCircularToplevelRoot control = new DualCircularToplevelRoot("foo", "bar");
    assertThat(persistor.fromXml(load("src/test/resources/dual-circular-toplevel-test.xml")))
      .isEqualToComparingFieldByFieldRecursively(control);
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="singleCircle", idField="id")
  @SuppressWarnings("unused")
  private static class SingleCircularToplevelRegenTest {
    private final String id = "foobar";
    
    @Persist("heyLookItsMe") private final SingleCircularToplevelRegenTest me;
    @Persist("dogInFrenchIs") private final String once;
    
    private SingleCircularToplevelRegenTest(String told) {
      me = this;
      once = told;
    }
  }
  
  @Test
  void singleCircularToplevelFromXml() throws Exception {
    XmlPersistor<SingleCircularToplevelRegenTest> persistor = new XmlPersistor<>(SingleCircularToplevelRegenTest.class);
    SingleCircularToplevelRegenTest control = new SingleCircularToplevelRegenTest("chien");
    assertThat(persistor.fromXml(load("src/test/resources/single-circular-toplevel-test.xml")))
      .isEqualToComparingFieldByFieldRecursively(control);
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="nullThings", idField="id")
  @SuppressWarnings({"unused", "UnusedAssignment"})
  private static class NullThingsRegenTest {
    private final String id = "foo";
    @Persist("nullNonToplevel") private NullThingRegen thing1 = new NullThingRegen("foo");
    @Persist("nonNullNonToplevel") private NullThingRegen thing2;
    @Persist("nullToplevel") private ToplevelNullThingRegen tlThing1 = new ToplevelNullThingRegen(2141);
    @Persist("nonNullToplevel") private ToplevelNullThingRegen tlThing2;
    private NullThingsRegenTest(NullThingRegen thing2, ToplevelNullThingRegen tlThing2) {
      this.thing1 = null;
      this.thing2 = thing2;
      this.tlThing1 = null;
      this.tlThing2 = tlThing2;
    }
  }
  
  @Persistable
  @SuppressWarnings({"unused", "UnusedAssignment"})
  private static class NullThingRegen {
    @Persist("thisIsNullString") private String nullThing = "ob";
    @Persist("thisHasValueNull") private String notNull;
    private NullThingRegen(String notNull) {
      this.nullThing = null;
      this.notNull = notNull;
    }
  }
  
  @Persistable(toplevel=true, tag="somethingElseNull", idField="myId")
  @SuppressWarnings({"unused", "UnusedAssignment"})
  private static class ToplevelNullThingRegen {
    private final int myId = 20141;
    @Persist("someInteger") private int whatever;
    @Persist("somethingNull") private String imNull = "this is not null";
    private ToplevelNullThingRegen(int whatever) {
      this.whatever = whatever;
      this.imNull = null;
    }
  }
  
  @Test
  void nullThingsFromXml() throws Exception {
    XmlPersistor<NullThingsRegenTest> persistor = new XmlPersistor<>(NullThingsRegenTest.class);
    NullThingsRegenTest control = new NullThingsRegenTest(new NullThingRegen("null"), new ToplevelNullThingRegen(-1));
    assertThat(persistor.fromXml(load("src/test/resources/null-things-test.xml")))
        .isEqualToComparingFieldByFieldRecursively(control);
  }
  
  // ==========================================================================================
  
  @Test
  void simpleNoPersistFieldsFromXml() throws Exception {
    XmlPersistor<SimpleNoPersistFieldsTest> persistor = new XmlPersistor<>(SimpleNoPersistFieldsTest.class);
    SimpleNoPersistFieldsTest control = new SimpleNoPersistFieldsTest();
    assertThat(persistor.fromXml(load("src/test/resources/simple-no-persist-fields-test.xml")))
        .isEqualToComparingFieldByFieldRecursively(control);
  }
  
  @Test
  void embeddedNoPersistFieldsFromXml() throws Exception {
    XmlPersistor<EmbeddedNoPersistFieldsTest> persistor = new XmlPersistor<>(EmbeddedNoPersistFieldsTest.class);
    EmbeddedNoPersistFieldsTest control = new EmbeddedNoPersistFieldsTest();
    control.e1 = new EmbeddedNoPersistFields();
    control.e2 = new EmbeddedNoPersistFields();
    assertThat(persistor.fromXml(load("src/test/resources/embedded-no-persist-fields-test.xml")))
        .isEqualToComparingFieldByFieldRecursively(control);
  }
  
  // ==========================================================================================
  // NEGATIVE TESTS - fromXml()
  
  @Persistable(toplevel=true, tag="thing", idField="id")
  @SuppressWarnings("unused")
  private static class CommonRegenTest {
    private final int id = 123;
    @Persist("foo") private final int bar = 1001;
    @Persist("thingy") private final CommonRegenMid test = new CommonRegenMid();
    @Persist("embedded") private final CommonRegenEmbedded embedded = new CommonRegenEmbedded();
  }
  
  @Persistable(toplevel=true, tag="thing2", idField="id")
  @SuppressWarnings("unused")
  private static class CommonRegenMid {
    private final int id = 456;
    @Persist("foo") private final String baz = "baz";
  }
  
  @Persistable
  @SuppressWarnings("unused")
  private static class CommonRegenEmbedded {
    private final int id = 789; // shouldn't actually be used
    @Persist("bar") private final String quux = "quux";
  }
  
  @Test
  void regenNoRootThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/no-root-test.xml")));
    assertThat(e).hasMessageContaining("No root");
  }
  
  @Test
  void regenMultipleRootsThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/multiple-roots-test.xml")));
    assertThat(e).hasMessageContaining("Multiple").hasMessageContaining("root");
  }
  
  @Test
  void regenRootButNoIdThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/no-id-test.xml")));
    assertThat(e).hasMessageContaining("id");
  }
  
  @Test
  void missingFieldThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/missing-field-test.xml")));
    assertThat(e).hasMessageContaining("Cannot find element with tag");
  }
  
  @Test
  void duplicateFieldThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/duplicate-field-test.xml")));
    assertThat(e).hasMessageContaining("Multiple elements with same parent with tag");
  }
  
  @Test
  void embeddingToplevelThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/embed-toplevel-test.xml")));
    assertThat(e).hasMessage("Cannot regenerate non-toplevel element to toplevel @Persistable class");
  }
  
  @Test
  void toplevelingEmbeddedThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/toplevel-embedded-test.xml")));
    assertThat(e).hasMessageContaining("toplevel reference to non-toplevel persistable class");
  }
  
  @Test
  void nonexistentToplevelIdThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/nonexistent-toplevel-id-test.xml")));
    assertThat(e).hasMessageContaining("id").hasMessageContaining("doesn't exist");
  }
  
  @Test
  void wrongIdTypeThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/wrong-id-type-test.xml")));
    assertThat(e).hasMessageContaining("Could not conform");
  }
  
  @Test
  void wrongPrimitiveFieldTypeThrows() {
    XmlPersistor<CommonRegenTest> persistor = new XmlPersistor<>(CommonRegenTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/wrong-primitive-type-test.xml")));
    assertThat(e).hasMessageContaining("Bad number format");
  }
  
  // ==========================================================================================
  
  @Persistable(toplevel=true, tag="charTest", idField="id")
  @SuppressWarnings("unused")
  private static class CharTest {
    private final double id = -41.5;
    @Persist("char") char myChar;
  }
  
  @Test
  void emptyCharTypeThrows() {
    XmlPersistor<CharTest> persistor = new XmlPersistor<>(CharTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/empty-char-test.xml")));
    assertThat(e).hasMessage("Trying to regenerate char with more than one character, or none");
  }
  
  @Test
  void overOneCharInCharTypeThrows() {
    XmlPersistor<CharTest> persistor = new XmlPersistor<>(CharTest.class);
    RegenerationException e = assertThrows(RegenerationException.class,
        () -> persistor.fromXml(load("src/test/resources/too-long-char-test.xml")));
    assertThat(e).hasMessage("Trying to regenerate char with more than one character, or none");
  }
  
}
