package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.database.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import java.time.Clock;

import static nl.knaw.huygens.timbuctoo.database.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDFINDEX_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class DatabaseRdfIndexTest {
  @Test
  public void indexTest() throws Exception {
    final TinkerpopGraphManager mgr = newGraph().wrap();
    final Database database = new Database(mgr);
    final DataStoreOperations dataStoreOperations =
      new DataStoreOperations(mgr, mock(ChangeListener.class), null, null, mock(HandleAdder.class));
    TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory =
      new TimbuctooActions.TimbuctooActionsFactory(mock(Authorizer.class), Clock.systemDefaultZone(),
        mock(HandleAdder.class));
    final TransactionEnforcer transactionEnforcer =
      new TransactionEnforcer(() -> dataStoreOperations, timbuctooActionsFactory);

    new ScaffoldMigrator(transactionEnforcer).execute();
    transactionEnforcer.execute(db -> {
      db.ensureVreExists("myVre");
      return commit();
    });
    mgr.getGraph().tx().onClose(
      x -> assertThat("Transaction should not be closed to be able to verify this unittest. It's okay that you close " +
        "a transaction during findOrCreateEntity, but you should find another way to verify that findOrCreateIndex " +
        "can find nodes in the index before a commit was called.", false)
    );
    database.findOrCreateEntity("myVre", NodeFactory.createURI("http://example.org/test"));

    //Index is created
    assertThat(mgr.getGraphDatabase().index().existsForNodes(RDFINDEX_NAME), is(true));
    //The nodes is available
    assertThat(
      mgr.getGraphDatabase().index().forNodes(RDFINDEX_NAME).get("myVre", "http://example.org/test").size(),
      is(1)
    );
  }
}
