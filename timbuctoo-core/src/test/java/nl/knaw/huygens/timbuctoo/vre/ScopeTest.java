package nl.knaw.huygens.timbuctoo.vre;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Place;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Test;

public class ScopeTest {

  @Test
  public void testBaseEntityTypes() throws IOException {
    TestScope scope = new TestScope();
    Set<Class<? extends DomainEntity>> types = scope.getBaseEntityTypes();
    assertTrue(types.contains(Person.class));
    assertFalse(types.contains(Place.class));
    assertFalse(types.contains(User.class));
  }

  @Test
  public void testAllEntityTypes() throws IOException {
    TestScope scope = new TestScope();
    Set<Class<? extends DomainEntity>> types = scope.getAllEntityTypes();
    assertTrue(types.contains(Person.class));
    assertTrue(types.contains(Place.class));
    assertFalse(types.contains(User.class));
  }

  private static class TestScope extends AbstractScope {

    public TestScope() throws IOException {
      addClass(Person.class);
      fixBaseTypes();
      addPackage("timbuctoo.model");
      fixAllTypes();
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
      return false;
    }
  }

}
