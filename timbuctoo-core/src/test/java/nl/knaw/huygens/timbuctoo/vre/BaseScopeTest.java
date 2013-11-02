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

public class BaseScopeTest {

  private static Scope scope;

  @BeforeClass
  public static void setupScope() throws IOException {
    scope = new BaseScope();
  }

  @Test
  public void testBaseEntityTypes() {
    assertTrue(scope.getBaseEntityTypes().contains(Person.class));
    assertTrue(scope.getBaseEntityTypes().contains(Place.class));
    assertFalse(scope.getBaseEntityTypes().contains(User.class));
    assertFalse(scope.getBaseEntityTypes().contains(DCARPerson.class));
    assertFalse(scope.getBaseEntityTypes().contains(CKCCPerson.class));
  }

  @Test
  public void testAllEntityTypes() {
    assertTrue(scope.getAllEntityTypes().contains(Person.class));
    assertTrue(scope.getAllEntityTypes().contains(Place.class));
    assertFalse(scope.getAllEntityTypes().contains(User.class));
    assertFalse(scope.getAllEntityTypes().contains(DCARPerson.class));
    assertFalse(scope.getAllEntityTypes().contains(CKCCPerson.class));
  }

  @Test
  public void testTypeAndIdInScope() {
    assertTrue(scope.inScope(Person.class, "id"));
    assertTrue(scope.inScope(Place.class, "id"));
    assertFalse(scope.inScope(DCARPerson.class, "id"));
    assertFalse(scope.inScope(CKCCPerson.class, "id"));
  }

  @Test
  public void testInstanceInScope() {
    assertTrue(scope.inScope(new Person()));
    assertTrue(scope.inScope(new Place()));
    assertFalse(scope.inScope(new DCARPerson()));
    assertFalse(scope.inScope(new CKCCPerson()));
  }

}
