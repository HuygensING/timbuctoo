package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.DatabaseConfiguredVres;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.jena.riot.Lang;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class RdfImporterTest {
  public static final String VRE_NAME = "vre";
  private static final String EXAMPLE_TRIPLE_STRING =
    "<http://tl.dbpedia.org/resource/Abadan,_Iran> " +
      "<http://www.w3.org/2003/01/geo/wgs84_pos#lat> " +
      "\"30.35\"^^<http://www.w3.org/2001/XMLSchema#float> .";

  @Test
  public void importRdfFirstCreatesAVreThanAddsTheTriplesToTheVre() {
    TinkerpopGraphManager graphWrapper = newGraph().wrap();
    DataStoreOperations db = mock(DataStoreOperations.class);
    TransactionEnforcer transactionEnforcer = createTransactionEnforcer(db);
    TripleProcessorImpl processor = mock(TripleProcessorImpl.class);
    RdfImporter instance = new RdfImporter(graphWrapper, VRE_NAME, mock(Vres.class), transactionEnforcer, processor);

    instance.importRdf(getTripleStream(EXAMPLE_TRIPLE_STRING), Lang.NQUADS);

    InOrder inOrder = inOrder(db, processor);
    inOrder.verify(db).ensureVreExists(VRE_NAME);
    inOrder.verify(processor).process(eq(VRE_NAME), eq(true), any());
  }

  private TransactionEnforcer createTransactionEnforcer(DataStoreOperations db) {
    TransactionEnforcer transactionEnforcer = new TransactionEnforcer(() -> db);
    return transactionEnforcer;
  }

  @Test
  public void importRdfReloadsTheDatabaseConfigurationAfterImport() {
    TransactionEnforcer transactionEnforcer = createTransactionEnforcer(mock(DataStoreOperations.class));
    TinkerpopGraphManager graphWrapper = newGraph().wrap();
    TripleProcessorImpl processor = mock(TripleProcessorImpl.class);
    final Vres vres = mock(DatabaseConfiguredVres.class);
    RdfImporter instance = new RdfImporter(graphWrapper, VRE_NAME, vres, transactionEnforcer, processor);

    instance.importRdf(getTripleStream(EXAMPLE_TRIPLE_STRING), Lang.NQUADS);

    InOrder inOrder = inOrder(processor, vres);
    inOrder.verify(processor).process(eq(VRE_NAME), eq(true), any());
    inOrder.verify(vres).reload();
  }


  private InputStream getTripleStream(String tripleString) {
    return new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
  }
}
