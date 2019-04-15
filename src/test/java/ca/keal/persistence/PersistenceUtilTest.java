package ca.keal.persistence;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceUtilTest {
  
  @ParameterizedTest(name = "isValidXmlTag should return true for valid XML tags")
  @ValueSource(strings = {"foo", "BaZ", "_0133", "____", "_", "a", "::", "a.b:c-d", "é", "ñéá4à."})
  void shouldBeValidXmlTags(String tag) {
    assertTrue(PersistenceUtil.isValidXmlTag(tag));
  }
  
  @ParameterizedTest(name = "isValidXmlTag should return false for invalid XML tags")
  @ValueSource(strings = {"", "0", " ", "a b", "@", "asfdijp^", "xml", "0a", "7a", "\\", "xmlfoobar", "-d", ".b"})
  void shouldBeInvalidXmlTags(String tag) {
    assertFalse(PersistenceUtil.isValidXmlTag(tag));
  }
  
  @Persistable
  private static class GoodPersistable1 {}
  
  @Persistable(toplevel = true, tag = "_someTag214.f-4é:", idField = "thisIdField")
  private static class GoodPersistable2 {
    @SuppressWarnings("unused") private String thisIdField;
  }
  
  @Persistable(tag = "4902374$*&(#$IvAlId(0))", idField = "%%%nonexistent###!!")
  private static class GoodPersistable3 {}
  
  @ParameterizedTest(name = "verifyAndGetPersistable should get persistable for valid @Persistable usages")
  @ValueSource(classes = {GoodPersistable1.class, GoodPersistable2.class, GoodPersistable3.class})
  void shouldGetGoodPersistables(Class<?> cls) {
    assertNotNull(PersistenceUtil.verifyAndGetPersistable(cls));
  }
  
  private static class BadPersistable1 {}
  
  @Persistable(toplevel = true)
  private static class BadPersistable2 {}
  
  @Persistable(toplevel = true, tag = "abc")
  private static class BadPersistable3 {}
  
  @Persistable(toplevel = true, idField = "fieldThatExists")
  private static class BadPersistable4 {
    @SuppressWarnings("unused") private int fieldThatExists;
  }
  
  @Persistable(toplevel = true, tag = "4902374$*&(#$IvAlId(0))", idField = "fieldThatExists")
  private static class BadPersistable5 {
    @SuppressWarnings("unused") private int fieldThatExists;
  }
  
  @Persistable(toplevel = true, tag = "abc", idField = "nonexistentField")
  private static class BadPersistable6 {}
  
  @ParameterizedTest(name = "verifyAndGetPersistable should throw PersistenceException for @Persistable invalid usages")
  @ValueSource(classes = {BadPersistable1.class, BadPersistable2.class, BadPersistable3.class, BadPersistable4.class,
      BadPersistable5.class, BadPersistable6.class})
  void shouldThrowForBadPersistables(Class<?> cls) {
    assertThrows(PersistenceException.class, () -> PersistenceUtil.verifyAndGetPersistable(cls));
  }
  
}
