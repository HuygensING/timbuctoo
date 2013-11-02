package nl.knaw.huygens.timbuctoo.vre;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Place;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.ckcc.CKCCPerson;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARPerson;

import org.junit.BeforeClass;
import org.junit.Test;

public class DutchCaribbeanScopeTest {

  private static Scope scope;

  @BeforeClass
  public static void setupScope() throws IOException {
    scope = new DutchCaribbeanScope();
  }

  @Test
  public void testBaseEntityTypes() {
    assertTrue(scope.getBaseEntityTypes().contains(Person.class));
    assertFalse(scope.getBaseEntityTypes().contains(Place.class));
    assertFalse(scope.getBaseEntityTypes().contains(User.class));
    assertFalse(scope.getBaseEntityTypes().contains(DCARPerson.class));
    assertFalse(scope.getBaseEntityTypes().contains(CKCCPerson.class));
  }

  @Test
  public void testAllEntityTypes() {
    assertFalse(scope.getAllEntityTypes().contains(Person.class));
    assertFalse(scope.getAllEntityTypes().contains(Place.class));
    assertFalse(scope.getAllEntityTypes().contains(User.class));
    assertTrue(scope.getAllEntityTypes().contains(DCARPerson.class));
    assertFalse(scope.getAllEntityTypes().contains(CKCCPerson.class));
  }

  @Test
  public void testTypeAndIdInScope() {
    assertFalse(scope.inScope(Person.class, "id"));
    assertFalse(scope.inScope(Place.class, "id"));
    assertTrue(scope.inScope(DCARPerson.class, "id"));
    assertFalse(scope.inScope(CKCCPerson.class, "id"));
  }

  @Test
  public void testInstanceInScopeBy() {
    assertFalse(scope.inScope(new Person()));
    assertFalse(scope.inScope(new Place()));
    assertTrue(scope.inScope(new DCARPerson()));
    assertFalse(scope.inScope(new CKCCPerson()));
  }

}
