package nl.knaw.huygens.timbuctoo.vre;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Place;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.ckcc.CKCCPerson;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARPerson;

import org.junit.Test;

public class DutchCaribbeanScopeTest {

  @Test
  public void testBaseEntityTypes() throws IOException {
    Scope scope = new DutchCaribbeanScope();
    assertTrue(scope.getBaseEntityTypes().contains(Person.class));
    assertFalse(scope.getBaseEntityTypes().contains(Place.class));
    assertFalse(scope.getBaseEntityTypes().contains(User.class));
    assertFalse(scope.getBaseEntityTypes().contains(DCARPerson.class));
    assertFalse(scope.getBaseEntityTypes().contains(CKCCPerson.class));
  }

  @Test
  public void testAllEntityTypes() throws IOException {
    Scope scope = new DutchCaribbeanScope();
    assertFalse(scope.getAllEntityTypes().contains(Person.class));
    assertFalse(scope.getAllEntityTypes().contains(Place.class));
    assertFalse(scope.getAllEntityTypes().contains(User.class));
    assertTrue(scope.getAllEntityTypes().contains(DCARPerson.class));
    assertFalse(scope.getAllEntityTypes().contains(CKCCPerson.class));
  }

}
