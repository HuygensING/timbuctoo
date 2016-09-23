package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.database.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

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
    final DataAccess dataAccess = new DataAccess(mgr, null, null, mock(ChangeListener.class), mock(HandleAdder.class));

    new ScaffoldMigrator(dataAccess).execute();
    dataAccess.execute(db -> db.ensureVreExists("myVre"));
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
