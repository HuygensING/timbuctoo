package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.NewTestRole;
import nl.knaw.huygens.timbuctoo.variation.model.TestRole;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.VTestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectANewTestRole;
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
    assertEquals(BaseDomainEntity.class, registry.getTypeForIName("basedomainentity"));
    assertEquals(VTestSystemEntity.class, registry.getTypeForIName("vtestsystementity"));
  }

  @Test
  public void testGetTypeForXName() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals(BaseDomainEntity.class, registry.getTypeForXName("basedomainentitys"));
    // Has @EntityTypeName annotation:
    assertEquals(VTestSystemEntity.class, registry.getTypeForXName("mysystementity"));
  }

  @Test
  public void testGetINameForType() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals("basedomainentity", registry.getINameForType(BaseDomainEntity.class));
  }

  @Test
  public void testGetINameForRole() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals("projectatestrole", registry.getINameForRole(ProjectATestRole.class));
  }

  @Test
  public void testGetINameEntity() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    assertEquals("basedomainentity", registry.getIName(BaseDomainEntity.class));
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
    assertEquals("basedomainentitys", registry.getXNameForType(BaseDomainEntity.class));
  }

  @Test
  public void testGetBaseClassFromCollectionClass() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(BaseDomainEntity.class, registry.getBaseClass(BaseDomainEntity.class));
  }

  @Test
  public void testGetBaseClassForProjectSpecificClass() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(BaseDomainEntity.class, registry.getBaseClass(ProjectADomainEntity.class));
  }

  @Test
  public void testGetBaseClassOfNull() {
    TypeRegistry registry = new TypeRegistry("");
    assertEquals(null, registry.getBaseClass(null));
  }

  @Test
  public void testGetCollectionIdForARegisteredClass() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE);
    Class<? extends Entity> baseType = registry.getBaseClass(BaseDomainEntity.class);
    assertEquals("basedomainentity", registry.getINameForType(baseType));
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

  @Test
  public void testGetAllowedRolesForModelPackage() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(0, registry.getAllowedRolesFor(TestSystemEntity.class).size());
    Set<Class<? extends Role>> roles = registry.getAllowedRolesFor(BaseDomainEntity.class);
    assertEquals(2, roles.size());
    assertTrue(roles.contains(TestRole.class));
    assertTrue(roles.contains(NewTestRole.class));
  }

  @Test
  public void testGetAllowedRolesForProjectPackage() {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    Set<Class<? extends Role>> roles = registry.getAllowedRolesFor(ProjectADomainEntity.class);
    assertEquals(2, roles.size());
    assertTrue(roles.contains(ProjectATestRole.class));
    assertTrue(roles.contains(ProjectANewTestRole.class));
  }

  // -------------------------------------------------------------------

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
