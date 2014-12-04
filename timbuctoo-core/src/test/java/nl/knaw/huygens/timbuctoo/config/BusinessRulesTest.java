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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
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

  // -------------------------------------------------------------------

  private static class Level1SystemEntity extends SystemEntity {
    @Override
    public String getIdentificationName() {
      return null;
    }
  }

  private static class Level2SystemEntity extends Level1SystemEntity {}

  private static class Level1DomainEntity extends DomainEntity {
    @Override
    public String getIdentificationName() {
      return null;
    }
  }

  private static class Level2DomainEntity extends Level1DomainEntity {}

  private static class Level3DomainEntity extends Level2DomainEntity {}

}
