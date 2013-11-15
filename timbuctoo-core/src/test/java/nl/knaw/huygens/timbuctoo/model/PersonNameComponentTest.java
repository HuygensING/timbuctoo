package nl.knaw.huygens.timbuctoo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Test;

import com.google.common.base.Objects;

public class PersonNameComponentTest {

  @Test
  public void testEqualsEqual() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = new PersonNameComponent(Type.FORENAME, "test");

    assertTrue(first.equals(second));
  }

  @Test
  public void testEqualsNoEqualType() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = new PersonNameComponent(Type.SURNAME, "test");

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsNoEqualValue() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = new PersonNameComponent(Type.FORENAME, "test1");

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsToNull() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = null;

    assertFalse(first.equals(second));
  }

  @Test
  public void testHashcode() {
    PersonNameComponent component = new PersonNameComponent(Type.FORENAME, "test");

    int expected = Objects.hashCode(Type.FORENAME, "test");

    assertEquals(expected, component.hashCode());

  }
}
