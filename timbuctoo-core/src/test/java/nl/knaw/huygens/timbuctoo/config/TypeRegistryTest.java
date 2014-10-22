package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.NewTestRole;
import test.variation.model.TestRole;
import test.variation.model.TestSystemEntity;
import test.variation.model.VTestSystemEntity;
import test.variation.model.projecta.ProjectADomainEntity;
import test.variation.model.projecta.ProjectANewTestRole;
import test.variation.model.projecta.ProjectATestRole;

public class TypeRegistryTest {

  private static final String PROJECT_A_MODEL = "test.variation.model.projecta";
  private static final String MODEL_PACKAGE = "test.variation.model";
  private static final String PACKAGE_WITH_INVALID_SYSTEM_ENTITY = " test.different.model.with_invalid_system_entity";
  private static final String PACKAGE_WITH_INVALID_DOMAIN_ENTITY = " test.different.model.with_invalid_domain_entity";
  private static final String PACKAGE_WITH_INVALID_ROLE = " test.different.model.with_invalid_role";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private TypeRegistry registry;

  @Before
  public void setup() {
    registry = TypeRegistry.getInstance();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPackageNamesMustnotBeNull() throws ModelException {
    registry.init(null);
  }

  @Test(expected = ModelException.class)
  public void testEntityTypeNamesMustBeDifferent() throws ModelException {
    registry.init(MODEL_PACKAGE + " " + MODEL_PACKAGE);
  }

  @Test
  public void testPackageWithInvalidSystemEntity() throws ModelException {
    exception.expect(ModelException.class);
    exception.expectMessage("InvalidSystemEntity is not a direct sub class of SystemEntity.");
    registry.init(PACKAGE_WITH_INVALID_SYSTEM_ENTITY);
  }

  @Test
  public void testPackageWithInvalidDomainEntity() throws ModelException {
    exception.expect(ModelException.class);
    exception.expectMessage("InvalidDomainEntity is not a direct sub class of DomainEntity or a sub class of a direct sub class of DomainEntity.");
    registry.init(PACKAGE_WITH_INVALID_DOMAIN_ENTITY);
  }

  @Test
  public void testPackageWithInvalidRole() throws ModelException {
    exception.expect(ModelException.class);
    exception.expectMessage("InvalidRole is not a direct sub class of Role or a sub class of a direct sub class of Role.");
    registry.init(PACKAGE_WITH_INVALID_ROLE);
  }

  @Test
  public void testRecursivePackageSpecification() throws ModelException {
    String iname = TypeNames.getInternalName(ProjectADomainEntity.class);
    registry.init(MODEL_PACKAGE);
    assertNull(registry.getDomainEntityType(iname));
    registry.init(MODEL_PACKAGE + ".*");
    assertEquals(ProjectADomainEntity.class, registry.getDomainEntityType(iname));
  }

  @Test
  public void testGetTypeForIName() throws ModelException {
    registry.init(MODEL_PACKAGE);
    assertEquals(BaseVariationDomainEntity.class, registry.getDomainEntityType("basevariationdomainentity"));
    assertEquals(VTestSystemEntity.class, registry.getSystemEntityType("vtestsystementity"));
  }

  @Test
  public void testGetTypeForXName() throws ModelException {
    registry.init(MODEL_PACKAGE);
    assertEquals(BaseVariationDomainEntity.class, registry.getTypeForXName("basevariationdomainentitys"));
  }

  @Test
  public void testGetBaseClassFromCollectionClass() {
    assertEquals(BaseVariationDomainEntity.class, TypeRegistry.getBaseClass(BaseVariationDomainEntity.class));
  }

  @Test
  public void testGetBaseClassForProjectSpecificClass() {
    assertEquals(BaseVariationDomainEntity.class, TypeRegistry.getBaseClass(ProjectADomainEntity.class));
  }

  @Test
  public void testGetBaseClassOfNull() {
    assertEquals(null, TypeRegistry.getBaseClass(null));
  }

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
    assertFalse(TypeRegistry.isSystemEntity(ADomainEntity.class));
    assertTrue(TypeRegistry.isSystemEntity(ASystemEntity.class));
    assertTrue(TypeRegistry.isSystemEntity(SystemEntity.class));

  }

  @Test
  public void testIsDomainEntity() {
    assertFalse(TypeRegistry.isDomainEntity(null));
    assertFalse(TypeRegistry.isDomainEntity(NotAnEntity.class));
    assertFalse(TypeRegistry.isDomainEntity(AnEntity.class));
    assertFalse(TypeRegistry.isDomainEntity(ASystemEntity.class));
    assertTrue(TypeRegistry.isDomainEntity(ADomainEntity.class));
    assertTrue(TypeRegistry.isDomainEntity(DomainEntity.class));
  }

  @Test
  public void testIsRole() {
    assertFalse(TypeRegistry.isRole((Class<?>) null));
    assertTrue(TypeRegistry.isRole(TestRole.class));
    assertTrue(TypeRegistry.isRole(ProjectATestRole.class));
    assertFalse(TypeRegistry.isRole(AnEntity.class));
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
  public void testToBaseDomainEntity() {
    assertEquals(BaseVariationDomainEntity.class, TypeRegistry.toBaseDomainEntity(BaseVariationDomainEntity.class));
    assertEquals(BaseVariationDomainEntity.class, TypeRegistry.toBaseDomainEntity(ProjectADomainEntity.class));
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
  public void testGetAllowedRolesForModelPackage() throws ModelException {
    registry.init(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
    assertEquals(0, registry.getAllowedRolesFor(TestSystemEntity.class).size());
    Set<Class<? extends Role>> roles = registry.getAllowedRolesFor(BaseVariationDomainEntity.class);
    assertEquals(2, roles.size());
    assertTrue(roles.contains(TestRole.class));
    assertTrue(roles.contains(NewTestRole.class));
  }

  @Test
  public void testGetAllowedRolesForProjectPackage() throws ModelException {
    registry.init(MODEL_PACKAGE + " " + PROJECT_A_MODEL);
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
