package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.DatabaseConfiguredVres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.riot.Lang;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class RdfImporterTest {
  public static final String VRE_NAME = "vre";
  private static final String EXAMPLE_TRIPLE_STRING =
    "<http://tl.dbpedia.org/resource/Abadan,_Iran> " +
      "<http://www.w3.org/2003/01/geo/wgs84_pos#lat> " +
      "\"30.35\"^^<http://www.w3.org/2001/XMLSchema#float> .";

  @Test
  public void importRdfCreatesAVreVertex() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    RdfImporter instance = new RdfImporter(graphWrapper, VRE_NAME, mock(Vres.class));

    instance.importRdf(getTripleStream(""), Lang.NQUADS);

    assertThat(
      graphWrapper.getGraph().traversal().V()
                  .hasLabel(Vre.DATABASE_LABEL)
                  .has(Vre.VRE_NAME_PROPERTY_NAME, VRE_NAME)
                  .hasNext(),
      is(true)
    );
  }

  @Test
  public void importRdfFirstCreatesAVreThanAddsTheTriplesToTheVre() {
    GraphWrapper graphWrapper = newGraph().wrap();
    TripleImporter tripleImporter = mock(TripleImporter.class);
    ImportPreparer importPreparer = mock(ImportPreparer.class);
    RdfImporter instance = new RdfImporter(graphWrapper, VRE_NAME, mock(Vres.class), tripleImporter, importPreparer);

    instance.importRdf(getTripleStream(EXAMPLE_TRIPLE_STRING), Lang.NQUADS);

    InOrder inOrder = inOrder(importPreparer, tripleImporter);
    inOrder.verify(importPreparer).setupVre(VRE_NAME);
    inOrder.verify(tripleImporter).importTriple(any());
  }

  @Test
  public void importRdfCreatesAnAdminVreAndConceptCollectionBeforeImportingTriples() {
    ImportPreparer importPreparer = mock(ImportPreparer.class);
    GraphWrapper graphWrapper = newGraph().wrap();
    TripleImporter tripleImporter = mock(TripleImporter.class);
    RdfImporter instance = new RdfImporter(graphWrapper, VRE_NAME, mock(Vres.class), tripleImporter, importPreparer);

    instance.importRdf(getTripleStream(EXAMPLE_TRIPLE_STRING), Lang.NQUADS);

    InOrder inOrder = inOrder(importPreparer, tripleImporter);
    inOrder.verify(importPreparer).setUpAdminVre();
    inOrder.verify(tripleImporter).importTriple(any());
  }

  @Test
  public void importRdfReloadsTheDatabaseConfigurationAfterImport() {
    ImportPreparer importPreparer = mock(ImportPreparer.class);
    GraphWrapper graphWrapper = newGraph().wrap();
    TripleImporter tripleImporter = mock(TripleImporter.class);
    final Vres vres = mock(DatabaseConfiguredVres.class);
    RdfImporter instance = new RdfImporter(graphWrapper, VRE_NAME, vres, tripleImporter, importPreparer);

    instance.importRdf(getTripleStream(EXAMPLE_TRIPLE_STRING), Lang.NQUADS);

    InOrder inOrder = inOrder(tripleImporter, vres);
    inOrder.verify(tripleImporter).importTriple(any());
    inOrder.verify(vres).reload();
  }


  private InputStream getTripleStream(String tripleString) {
    return new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
  }
}
