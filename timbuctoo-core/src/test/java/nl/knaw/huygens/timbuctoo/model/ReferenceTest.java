package nl.knaw.huygens.timbuctoo.model;

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
