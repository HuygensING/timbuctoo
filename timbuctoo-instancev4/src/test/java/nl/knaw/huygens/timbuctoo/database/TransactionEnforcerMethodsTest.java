package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TransactionEnforcerMethodsTest {

  @Test
  public void emptyDatabaseIsShownAsEmpty() throws Exception {
    TransactionEnforcer transactionEnforcer = new TransactionEnforcer(
      () -> new DataStoreOperations(newGraph().wrap(), null, null, null, null));
    try (DataStoreOperations db = transactionEnforcer.start()) {
      assertThat(db.databaseIsEmptyExceptForMigrations(), is(true));
    }
  }

  @Test
  public void nonEmptyDatabaseIsShownAsFull() throws Exception {
    TransactionEnforcer transactionEnforcer = new TransactionEnforcer(() -> new DataStoreOperations(newGraph()
        .withVertex(v -> v
          .withTimId(UUID.randomUUID().toString())
        ).wrap(), null, null, null, null));
    try (DataStoreOperations db = transactionEnforcer.start()) {
      assertThat(db.databaseIsEmptyExceptForMigrations(), is(false));
    }
  }

  @Test
  public void ensureVreExistsCreatesAVreIfNeeded() throws Exception {
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
      ).wrap();
    TransactionEnforcer transactionEnforcer = new TransactionEnforcer(
      () -> new DataStoreOperations(graphWrapper, null, null, null, null));
    try (DataStoreOperations db = transactionEnforcer.start()) {
      db.ensureVreExists("SomeVre");
      assertThat(
        graphWrapper.getGraph().traversal().V()
                    .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                    .has(Vre.VRE_NAME_PROPERTY_NAME, "SomeVre")
                    .hasNext(),
        is(true)
      );
    }

  }
}
