package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestBaseDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestInheritsFromTestBaseDoc;

import org.junit.Test;

/**
 * Tests for the TypeRegistry. Watch-out the register is highly
 * dependent on the getCollectionName method. When that
 * implementation changes, a lot of tests will fail.
 * 
 * TODO: remove the dependency of the model-package, in these tests.
 */
public class TypeRegistryTest {

  private static final String MODEL_PACKAGE = "timbuctoo.variation.model";

  @Test(expected = IllegalArgumentException.class)
  public void testPackageNamesMustnotBeNull() {
    new TypeRegistry(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testEntityTypeNamesMustBeDifferent() {
    new TypeRegistry(MODEL_PACKAGE + " " + MODEL_PACKAGE);
  }

  @Test
  public void testGetTypeForIName() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(GeneralTestDoc.class, registry.getTypeForIName("generaltestdoc"));
    assertEquals(TestExtraBaseDoc.class, registry.getTypeForIName("testextrabasedoc"));
  }

  @Test
  public void testGetTypeForXName() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(GeneralTestDoc.class, registry.getTypeForXName("generaltestdocs"));
    // TestExtraBaseDoc has @EntityTypeName("testextrabasedoc")
    assertEquals(TestExtraBaseDoc.class, registry.getTypeForXName("testextrabasedoc"));
  }

  @Test
  public void testBaseClassFromCollectionBaseClass() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(TestBaseDoc.class, registry.getBaseClass(TestBaseDoc.class));
  }

  @Test
  public void testBaseClassFromCollectionClass() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(TestConcreteDoc.class, registry.getBaseClass(TestConcreteDoc.class));
  }

  @Test
  public void testGetBaseClassFromNonDirectDescendantOfDocument() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(TestInheritsFromTestBaseDoc.class, registry.getBaseClass(TestInheritsFromTestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdForARegisteredClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    Class<? extends Entity> baseType = registry.getBaseClass(TestConcreteDoc.class);
    assertEquals("testconcretedoc", registry.getINameForType(baseType));
  }

  @Test
  public void testRegisterPackageDontRegisterClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("donotregistertests"));
  }

  @Test
  public void testRegisterPackageNonDocument() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("nonDoc"));
  }

  @Test
  public void testRegisterPackageSubPackage() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("testdoc"));
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("testbasedoc"));
  }

  // --- tests of static utilities -------------------------------------

  @Test
  public void testIsEntity() {
    assertFalse(TypeRegistry.isEntity(NotAnEntity.class));
    assertTrue(TypeRegistry.isEntity(AnEntity.class));
    assertTrue(TypeRegistry.isEntity(ASystemEntity.class));
    assertTrue(TypeRegistry.isEntity(ADomainEntity.class));
  }

  @Test
  public void testIsSystemEntity() {
    assertFalse(TypeRegistry.isSystemEntity(NotAnEntity.class));
    assertFalse(TypeRegistry.isSystemEntity(AnEntity.class));
    assertTrue(TypeRegistry.isSystemEntity(ASystemEntity.class));
    assertFalse(TypeRegistry.isSystemEntity(ADomainEntity.class));
  }

  @Test
  public void testIsDomainEntity() {
    assertFalse(TypeRegistry.isDomainEntity(NotAnEntity.class));
    assertFalse(TypeRegistry.isDomainEntity(AnEntity.class));
    assertFalse(TypeRegistry.isDomainEntity(ASystemEntity.class));
    assertTrue(TypeRegistry.isDomainEntity(ADomainEntity.class));
  }

  @Test(expected = ClassCastException.class)
  public void testToEntityFails() {
    TypeRegistry.toEntity(NotAnEntity.class);
  }

  @Test
  public void testToEntitySucceeds() {
    assertTrue(TypeRegistry.isEntity(TypeRegistry.toEntity(ASystemEntity.class)));
  }

  @Test(expected = ClassCastException.class)
  public void testToSystemEntityFails() {
    TypeRegistry.toSystemEntity(ADomainEntity.class);
  }

  @Test
  public void testToSystemEntitySucceeds() {
    assertTrue(TypeRegistry.isSystemEntity(TypeRegistry.toSystemEntity(ASystemEntity.class)));
  }

  @Test(expected = ClassCastException.class)
  public void testToDomainEntityFails() {
    TypeRegistry.toDomainEntity(ASystemEntity.class);
  }

  @Test
  public void testToDomainEntitySucceeds() {
    assertTrue(TypeRegistry.isDomainEntity(TypeRegistry.toDomainEntity(ADomainEntity.class)));
  }

  private static class NotAnEntity {}

  private static class AnEntity extends Entity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class ASystemEntity extends SystemEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class ADomainEntity extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

}
