package nl.knaw.huygens.timbuctoo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class PersonNameTest {

  @Test
  public void testEqualsEqual() {
    PersonName first = createPersonName("test", "test2");
    PersonName second = createPersonName("test", "test2");

    assertTrue(first.equals(second));
  }

  protected PersonName createPersonName(String forename, String surname) {
    PersonName personName = new PersonName();
    personName.addNameComponent(Type.FORENAME, forename);
    personName.addNameComponent(Type.SURNAME, surname);

    return personName;
  }

  @Test
  public void testEqualsNotEqual() {
    PersonName first = createPersonName("test", "test2");
    PersonName second = createPersonName("test1", "test2");

    assertFalse(first.equals(second));
  }

  @Test
  public void testHashCode() {
    PersonName personName = createPersonName("test", "test2");

    PersonNameComponent foreName = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent surname = new PersonNameComponent(Type.SURNAME, "test2");

    List<PersonNameComponent> components = Lists.newArrayList(foreName, surname);

    assertEquals(Objects.hashCode(components), personName.hashCode());
  }

}
