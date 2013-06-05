package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.variation.model.TestExtraBaseDoc;

import org.junit.Test;

public class TypeConverterTest {

  private TypeConverter setupConverter(String packages) {
    return new TypeConverter(new DocTypeRegistry(packages));
  }

  @Test
  public void testGetClassFromMongoTypeStringeAllLowerCase() {
    TypeConverter converter = setupConverter("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, converter.getClass("testextrabasedoc"));
  }

  @Test
  public void testGetClassFromMongoTypeStringWithCapitals() {
    TypeConverter converter = setupConverter("nl.knaw.huygens.repository.variation.model");
    assertNull(converter.getClass("TestExtraBaseDocs"));
  }

  @Test
  public void testGetClassFromMongoTypeStringAllUppercase() {
    TypeConverter converter = setupConverter("nl.knaw.huygens.repository.variation.model");
    assertNull(converter.getClass("TESTEXTRABASEDOCs"));
  }

  @Test
  public void testGetClassFromMongoTypeStringWithPackage() {
    TypeConverter converter = setupConverter("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, converter.getClass("model-testextrabasedoc"));
  }

}
