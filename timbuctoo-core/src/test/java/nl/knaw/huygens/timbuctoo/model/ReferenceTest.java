package nl.knaw.huygens.timbuctoo.model;

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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntityPrimitive;

import org.junit.Test;

import test.model.projecta.SubADomainEntity;

public class ReferenceTest {
  @Test
  public void testEqualsEqual() {
    Reference ref1 = new Reference(TestSystemEntity.class, "id");
    Reference ref2 = new Reference(TestSystemEntity.class, "id");

    assertEquals(ref1, ref2);
  }

  @Test
  public void testEqualsDifferentType() {
    Reference ref1 = new Reference(TestSystemEntityPrimitive.class, "id");
    Reference ref2 = new Reference(TestSystemEntity.class, "id");

    assertNotSame(ref1, ref2);
  }

  @Test
  public void testEqualsEqualDifferentId() {
    Reference ref1 = new Reference(TestSystemEntity.class, "id1");
    Reference ref2 = new Reference(TestSystemEntity.class, "id2");

    assertNotSame(ref1, ref2);
  }

  @Test
  public void testHashCode() {
    Reference ref1 = new Reference(TestSystemEntity.class, "id1");
    int expected = -127126941;

    int actual = ref1.hashCode();

    assertEquals(expected, actual);
  }

  @Test
  public void testHashCodeIdNull() {
    Reference ref1 = new Reference(TestSystemEntity.class, null);
    int expected = -127230995;

    int actual = ref1.hashCode();

    assertEquals(expected, actual);
  }

  @Test
  public void testIsOfTypeSameType() {
    Reference ref1 = new Reference(TestSystemEntity.class, "id1");

    assertTrue(ref1.refersToType(TestSystemEntity.class));
  }

  @Test
  public void testIsOfTypeSubType() {
    Reference ref1 = new Reference(BaseDomainEntity.class, "id1");

    assertFalse(ref1.refersToType(SubADomainEntity.class));
  }

  @Test
  public void testIsOfTypeSupeType() {
    Reference ref1 = new Reference(SubADomainEntity.class, "id1");

    assertFalse(ref1.refersToType(BaseDomainEntity.class));
  }

  @Test
  public void testIsOfTypeOtherType() {
    Reference ref1 = new Reference(TestSystemEntity.class, "id1");

    assertFalse(ref1.refersToType(BaseDomainEntity.class));
  }

}
