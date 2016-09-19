package nl.knaw.huygens.timbuctoo.database;

import org.junit.Test;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DataAccessMethodsTest {

  @Test
  public void emptyDatabaseIsShownAsEmpty() throws Exception {
    DataAccess dataAccess = new DataAccess(newGraph().wrap(), null, null, null, null);
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      assertThat(db.databaseIsEmptyExceptForMigrations(), is(true));
    }
  }

  @Test
  public void nonEmptyDatabaseIsShownAsFull() throws Exception {
    DataAccess dataAccess = new DataAccess(newGraph()
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
      ).wrap(),
      null,
      null,
      null,
      null);
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      assertThat(db.databaseIsEmptyExceptForMigrations(), is(false));
    }
  }

}
