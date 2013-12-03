package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Test;

public class BusinessRulesTest {

  @Test
  public void testIsValidSystemEntity() {
    assertFalse(BusinessRules.isValidSystemEntity(null));
    assertFalse(BusinessRules.isValidSystemEntity(String.class));
    assertFalse(BusinessRules.isValidSystemEntity(SystemEntity.class));
    assertTrue(BusinessRules.isValidSystemEntity(Level1SystemEntity.class));
    assertFalse(BusinessRules.isValidSystemEntity(Level2SystemEntity.class));
    assertFalse(BusinessRules.isValidSystemEntity(Level1DomainEntity.class));
  }

  @Test
  public void testIsValidDomainEntity() {
    assertFalse(BusinessRules.isValidDomainEntity(null));
    assertFalse(BusinessRules.isValidDomainEntity(String.class));
    assertFalse(BusinessRules.isValidDomainEntity(DomainEntity.class));
    assertTrue(BusinessRules.isValidDomainEntity(Level1DomainEntity.class));
    assertTrue(BusinessRules.isValidDomainEntity(Level2DomainEntity.class));
    assertFalse(BusinessRules.isValidDomainEntity(Level3DomainEntity.class));
    assertFalse(BusinessRules.isValidDomainEntity(Level1SystemEntity.class));
  }

  @Test
  public void testIsValidRole() {
    assertFalse(BusinessRules.isValidRole(null));
    assertFalse(BusinessRules.isValidRole(String.class));
    assertFalse(BusinessRules.isValidRole(Role.class));
    assertTrue(BusinessRules.isValidRole(Level1Role.class));
    assertTrue(BusinessRules.isValidRole(Level2Role.class));
    assertFalse(BusinessRules.isValidRole(Level3Role.class));
  }

  @Test
  public void testAllowSystemEntityAdd() {
    assertFalse(BusinessRules.allowSystemEntityAdd(null));
    assertFalse(BusinessRules.allowSystemEntityAdd(SystemEntity.class));
    assertTrue(BusinessRules.allowSystemEntityAdd(Level1SystemEntity.class));
    assertFalse(BusinessRules.allowSystemEntityAdd(Level2SystemEntity.class));
  }

  @Test
  public void testAllowDomainEntityAdd() {
    assertFalse(BusinessRules.allowDomainEntityAdd(null));
    assertFalse(BusinessRules.allowDomainEntityAdd(DomainEntity.class));
    assertFalse(BusinessRules.allowDomainEntityAdd(Level1DomainEntity.class));
    assertTrue(BusinessRules.allowDomainEntityAdd(Level2DomainEntity.class));
    assertFalse(BusinessRules.allowDomainEntityAdd(Level3DomainEntity.class));
  }

  @Test
  public void testAllowRoleAdd() {
    assertFalse(BusinessRules.allowRoleAdd(null));
    assertFalse(BusinessRules.allowRoleAdd(Role.class));
    assertFalse(BusinessRules.allowRoleAdd(Level1Role.class));
    assertTrue(BusinessRules.allowRoleAdd(Level2Role.class));
    assertFalse(BusinessRules.allowRoleAdd(Level3Role.class));
  }

  // -------------------------------------------------------------------

  private static class Level1SystemEntity extends SystemEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class Level2SystemEntity extends Level1SystemEntity {}

  private static class Level1DomainEntity extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class Level2DomainEntity extends Level1DomainEntity {}

  private static class Level3DomainEntity extends Level2DomainEntity {}

  private class Level1Role extends Role {}

  private class Level2Role extends Level1Role {}

  private class Level3Role extends Level2Role {}

}
