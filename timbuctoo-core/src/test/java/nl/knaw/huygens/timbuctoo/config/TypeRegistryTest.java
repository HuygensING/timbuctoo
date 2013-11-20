package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestRole;
import nl.knaw.huygens.timbuctoo.variation.model.VTestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectATestRole;

import org.junit.Test;

/**
 * Tests for the TypeRegistry. Watch-out the register is highly
 * dependent on the getCollectionName method. When that
 * implementation changes, a lot of tests will fail.
 * 
 * TODO: remove the dependency of the model-package, in these tests.
 */
public class TypeRegistryTest {

  private static final String PROJECT_A_MODEL = "timbuctoo.variation.model.projecta";
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
    assertEquals(VTestSystemEntity.class, registry.getTypeForIName("vtestsystementity"));
  }

  @Test
  public void testGetRoleForIName() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(ProjectATestRole.class, registry.getRoleForIName("projectatestrole"));
  }

  @Test
  public void testGetForINameRole() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(ProjectATestRole.class, registry.getForIName("projectatestrole"));
  }

  @Test
  public void testGetForINameEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(VTestSystemEntity.class, registry.getForIName("vtestsystementity"));
  }

  @Test
  public void testGetTypeForXName() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(GeneralTestDoc.class, registry.getTypeForXName("generaltestdocs"));
    // Has @EntityTypeName annotation:
    assertEquals(VTestSystemEntity.class, registry.getTypeForXName("mysystementity"));
  }

  @Test
  public void testGetINameForType() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals("generaltestdoc", registry.getINameForType(GeneralTestDoc.class));
  }

  @Test
  public void testGetINameForRole() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals("projectatestrole", registry.getINameForRole(ProjectATestRole.class));
  }

  @Test
  public void testGetINameEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals("generaltestdoc", registry.getIName(GeneralTestDoc.class));
  }

  @Test
  public void testGetINameRole() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals("projectatestrole", registry.getIName(ProjectATestRole.class));
  }

  @Test
  public void testGetINameNonDomainClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(null, registry.getIName(String.class));
  }

  @Test
  public void testGetXNameForType() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals("generaltestdocs", registry.getXNameForType(GeneralTestDoc.class));
  }

  @Test
  public void testGetBaseClassFromCollectionClass() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(GeneralTestDoc.class, registry.getBaseClass(GeneralTestDoc.class));
  }

  @Test
  public void testGetBaseClassForProjectSpecificClass() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(GeneralTestDoc.class, registry.getBaseClass(ProjectADomainEntity.class));
  }

  @Test
  public void testGetBaseClassOfNull() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(null, registry.getBaseClass(null));
  }

  @Test
  public void testGetBaseRoleOfBaseRole() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(TestRole.class, registry.getBaseRole(TestRole.class));
  }

  @Test
  public void testGetBaseRoleOfProjectSpecificBaseRole() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(TestRole.class, registry.getBaseRole(ProjectATestRole.class));
  }

  @Test
  public void testGetBaseRoleOfNull() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(null, registry.getBaseRole(null));
  }

  @Test
  public void testGetBaseOfBaseRole() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(TestRole.class, registry.getBase(ProjectATestRole.class));
  }

  @Test
  public void testGetBaseOfNull() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(null, registry.getBase(null));
  }

  @Test
  public void testGetBaseOfOtherType() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(null, registry.getBase(String.class));
  }

  @Test
  public void testGetBaseClassOfEntity() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(GeneralTestDoc.class, registry.getBase(ProjectADomainEntity.class));
  }

  @Test
  public void testGetSubClassesOfProjectSubClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertNull(registry.getSubClasses(ProjectADomainEntity.class));
  }

  @Test
  public void testGetSubClassesOfDomainEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertNull(registry.getSubClasses(DomainEntity.class));
  }

  @Test
  public void testGetSubClassesOfSystemEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertNull(registry.getSubClasses(SystemEntity.class));
  }

  @Test
  public void testGetSubClassesOfEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertNull(registry.getSubClasses(Entity.class));
  }

  @Test
  public void testGetCollectionIdForARegisteredClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    Class<? extends Entity> baseType = registry.getBaseClass(GeneralTestDoc.class);
    assertEquals("generaltestdoc", registry.getINameForType(baseType));
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
  public void testGetClassVariation() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals("projecta", registry.getClassVariation(ProjectADomainEntity.class));
  }

  @Test
  public void testGetClassVariationForRole() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals("projecta", registry.getClassVariation(ProjectATestRole.class));
  }

  @Test
  public void testGetClassVariationForPrimitive() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(null, registry.getClassVariation(Person.class));
  }

  @Test
  public void testGetClassVariationForDirectSubClassOfEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(null, registry.getClassVariation(DomainEntity.class));
  }

  @Test
  public void testGetVariationClassForDomainEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(ProjectADomainEntity.class, registry.getVariationClass(GeneralTestDoc.class, "projecta"));
  }

  @Test
  public void testGetVariationClassForRole() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(ProjectATestRole.class, registry.getVariationClass(TestRole.class, "projecta"));
  }

  @Test
  public void testGetVariationClassNotFound() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(null, registry.getVariationClass(TestRole.class, "nonExistionVariation"));
  }

  @Test
  public void testGetVariationClassVariationNull() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(null, registry.getVariationClass(TestRole.class, null));
  }

  // --- tests of static utilities -------------------------------------

  @Test
  public void testIsEntity() {
    assertFalse(TypeRegistry.isEntity(null));
    assertFalse(TypeRegistry.isEntity(NotAnEntity.class));
    assertTrue(TypeRegistry.isEntity(AnEntity.class));
    assertTrue(TypeRegistry.isEntity(ASystemEntity.class));
    assertTrue(TypeRegistry.isEntity(ADomainEntity.class));
  }

  @Test
  public void testIsSystemEntity() {
    assertFalse(TypeRegistry.isSystemEntity(null));
    assertFalse(TypeRegistry.isSystemEntity(NotAnEntity.class));
    assertFalse(TypeRegistry.isSystemEntity(AnEntity.class));
    assertTrue(TypeRegistry.isSystemEntity(ASystemEntity.class));
    assertFalse(TypeRegistry.isSystemEntity(ADomainEntity.class));
  }

  @Test
  public void testIsDomainEntity() {
    assertFalse(TypeRegistry.isDomainEntity(null));
    assertFalse(TypeRegistry.isDomainEntity(NotAnEntity.class));
    assertFalse(TypeRegistry.isDomainEntity(AnEntity.class));
    assertFalse(TypeRegistry.isDomainEntity(ASystemEntity.class));
    assertTrue(TypeRegistry.isDomainEntity(ADomainEntity.class));
  }

  @Test
  public void testIsRole() {
    assertFalse(TypeRegistry.isRole((Class<?>) null));
    assertTrue(TypeRegistry.isRole(TestRole.class));
    assertTrue(TypeRegistry.isRole(ProjectATestRole.class));
    assertFalse(TypeRegistry.isRole(AnEntity.class));
  }

  @Test
  public void testIsRoleWithStrings() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertTrue(registry.isRole("testrole"));
    assertTrue(registry.isRole("projectatestrole"));
    assertFalse(registry.isRole("anentity"));
    assertFalse(registry.isRole((String) null));
  }

  @Test
  public void testIsVariable() {
    assertTrue(TypeRegistry.isVariable(TestRole.class));
    assertTrue(TypeRegistry.isVariable(ProjectATestRole.class));
    assertFalse(TypeRegistry.isVariable(AnEntity.class));
    assertTrue(TypeRegistry.isVariable(DomainEntity.class));
    assertFalse(TypeRegistry.isVariable(null));
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

  @Test
  public void testToRole() {
    assertTrue(TypeRegistry.isRole(TypeRegistry.toRole(TestRole.class)));
  }

  @Test(expected = ClassCastException.class)
  public void testToRoleFail() {
    TypeRegistry.toRole(ADomainEntity.class);
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
