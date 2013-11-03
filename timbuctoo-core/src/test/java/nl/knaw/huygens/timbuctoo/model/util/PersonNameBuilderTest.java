package nl.knaw.huygens.timbuctoo.model.util;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Test;

public class PersonNameBuilderTest {

  @Test
  public void testSeparator() {
    assertEquals("", PersonNameBuilder.separator(null, null));
    assertEquals("", PersonNameBuilder.separator(Type.FORENAME, null));
    assertEquals("", PersonNameBuilder.separator(null, Type.SURNAME));
    assertEquals(" ", PersonNameBuilder.separator(Type.FORENAME, Type.SURNAME));
    assertEquals(", ", PersonNameBuilder.separator(Type.SURNAME, Type.FORENAME));
  }

  @Test
  public void testOneComponent() {
    PersonNameBuilder builder = new PersonNameBuilder();
    builder.addComponent(new PersonNameComponent(Type.FORENAME, "Christiaan"));
    assertEquals("Christiaan", builder.getName());
  }

  @Test
  public void testTwoComponents() {
    PersonNameBuilder builder = new PersonNameBuilder();
    builder.addComponent(new PersonNameComponent(Type.FORENAME, "Christiaan"));
    builder.addComponent(new PersonNameComponent(Type.SURNAME, "Huygens"));
    assertEquals("Christiaan Huygens", builder.getName());
  }

  @Test
  public void testTwoComponentsReversed() {
    PersonNameBuilder builder = new PersonNameBuilder();
    builder.addComponent(new PersonNameComponent(Type.SURNAME, "Huygens"));
    builder.addComponent(new PersonNameComponent(Type.FORENAME, "Christiaan"));
    assertEquals("Huygens, Christiaan", builder.getName());
  }

}
