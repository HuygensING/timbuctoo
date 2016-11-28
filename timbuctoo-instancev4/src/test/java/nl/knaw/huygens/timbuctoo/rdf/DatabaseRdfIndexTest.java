package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.database.TransactionEnforcerStubs.forGraphWrapper;
import static nl.knaw.huygens.timbuctoo.database.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDFINDEX_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DatabaseRdfIndexTest {
  @Test
  public void indexTest() throws Exception {
    final TinkerpopGraphManager mgr = newGraph().wrap();
    final Database database = new Database(mgr);
    TransactionEnforcer transactionEnforcer = forGraphWrapper(mgr);

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
