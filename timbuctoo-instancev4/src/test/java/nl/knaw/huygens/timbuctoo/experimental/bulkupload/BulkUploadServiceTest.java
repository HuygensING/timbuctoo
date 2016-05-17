package nl.knaw.huygens.timbuctoo.experimental.bulkupload;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.rangebasedxlsloader.RangebasedXlsLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.Saver;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.experimental.bulkupload.BulkUploadServiceTest.SuccessVerifier.withSheet;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkUploadServiceTest {

  private static Vres VRES = new Vres.Builder()
    .withVre("EuropeseMigratie", "em", vre -> vre
      .withCollection("emcards", coll -> coll
        .withAbstractType("document")
        .withProperty("naam", localProperty("emcardcatalogs_name"))
      )
      .withCollection("emcardcatalogs", coll -> coll
        .withAbstractType("collective")
        .withProperty("naam", localProperty("emcardcatalogs_name"))
        .withProperty("type", localProperty("emcardcatalogs_type",
          Converters.stringToUnencodedStringOf("valide", "ook valide")))

      )
    ).build();

  private Saver mockSaver(Vre vre) {
    final Saver mock = mock(Saver.class);
    given(mock.checkLeftoverCollectionsThatWereExpected()).willReturn(Optional.empty());
    given(mock.checkLeftoverVerticesThatWereExpected(any())).willReturn(Optional.empty());
    given(mock.checkRelationtypesThatWereExpected()).willReturn(Optional.empty());
    given(mock.getVre()).willReturn(vre);
    given(mock.makeRelation(any(), any(), any(), any(), any())).willReturn(Optional.empty());
    given(mock.setVertexProperties(any(), any(), any())).willReturn(mock(Vertex.class));
    return mock;
  }

  @Test
  public void loadOneCollection() throws Exception {
    final String filename = "loadOneCollection.xlsx";
    final RangebasedXlsLoader loader = new RangebasedXlsLoader();
    final Saver saver = mockSaver(VRES.getVre("EuropeseMigratie"));
    loader.loadWorkbookAndMarkErrors(getWorkbook(filename), new Importer(saver));
    withSheet()
      .withCollection(5, c->c
        .withColumn()
      )
      .verifyCounts(loader);
  }

  @Test
  @Ignore
  public void withFailingValues() throws Exception {
    Graph graph = newGraph().build();
    final String filename = "withFailingValues.xlsx";
    final BulkUploadService instance = new BulkUploadService(VRES, mockWrapper(graph));
    final XSSFWorkbook workbook = getWorkbook(filename);
    instance.saveToDb("EuropeseMigratie", workbook);
    final List<Vertex> vertices = graph.traversal().V().toList();
    assertThat(vertices.size(), is(5));
    final List<Vertex> verticesWithData = graph.traversal().V().has("emcardcatalogs_type").toList();
    assertThat(verticesWithData.size(), is(2)); //three have a failing input
    //uncomment lines below to look at the visual output
    //FileOutputStream out = new FileOutputStream("example_output.xlsx");
    //workbook.write(out);
  }

  @Test
  public void loadingTwice() throws Exception {
    Graph graph = newGraph().build();
    final String filename = "loadingTwice.xlsx";
    final BulkUploadService instance = new BulkUploadService(VRES, mockWrapper(graph));
    instance.saveToDb("EuropeseMigratie", getWorkbook(filename));
    instance.saveToDb("EuropeseMigratie", getWorkbook(filename));
    final List<Vertex> vertices = graph.traversal().V().toList();
    assertThat(vertices.size(), is(5));
  }

  @Test
  public void loadRelations() throws Exception {
    Graph graph = newGraph().build();

    final String filename = "loadRelations.xlsx";
    final RangebasedXlsLoader loader = new RangebasedXlsLoader();
    final Saver saver = new TinkerpopSaver(
      mockWrapper(graph),
      VRES.getVre("EuropeseMigratie"),
      RelationDescription.bothWays("isStoredAt", "contains", "document", "collective", false, false, false),
      100
    );
    loader.loadWorkbookAndMarkErrors(getWorkbook(filename), new Importer(saver));
    withSheet()
      .withCollection(5, c->c
        .withColumn()
      )
      .withCollection(2, c->c
        .withColumn()
        .withColumn()
      )
      .verifyCounts(loader);

    final List<Vertex> vertices = graph.traversal().V().toList();
    assertThat(vertices.size(), is(7));
    final List<Vertex> verticesWithOutgoingRelations = graph.traversal().V().out().toList();
    assertThat(verticesWithOutgoingRelations.size(), is(2));
  }

  private XSSFWorkbook getWorkbook(String filename) throws IOException {
    return new XSSFWorkbook(getClass().getResource(filename).getFile());
  }

  private GraphWrapper mockWrapper(Graph graph) {
    GraphWrapper result = mock(GraphWrapper.class);
    when(result.getGraph()).thenReturn(graph);
    return result;
  }


  public static class SuccessVerifier {
    private int successes = 1; //For ending the sheet
    private int failures = 0;
    private int ignoreds = 0;

    public static SuccessVerifier withSheet() {
      return new SuccessVerifier();
    }

    public SuccessVerifier withCollection(int rowCount, Consumer<CollectionVerifier> consumer) {
      final CollectionVerifier collectionVerifier = new CollectionVerifier(rowCount);
      consumer.accept(collectionVerifier);
      successes += collectionVerifier.getSuccesses();
      failures += collectionVerifier.getFailures();
      ignoreds += collectionVerifier.getIgnoreds();
      return this;
    }

    public void verifyCounts(RangebasedXlsLoader loader) {
      assertThat(loader.getSuccesses(), is(successes));
      assertThat(loader.getFailures(), is(failures));
      assertThat(loader.getIgnoreds(), is(ignoreds));
    }
  }

  private static class CollectionVerifier {
    private int successes;
    private int failures = 0;
    private int ignoreds = 0;
    private final int headerRows = 1;
    private final int rowCount;

    private CollectionVerifier(int rowCount) {
      successes = 2; //For starting and ending the collection
      this.rowCount = rowCount;
    }

    public CollectionVerifier withColumn() {
      successes += headerRows;
      successes += rowCount;
      return this;
    }

    public CollectionVerifier withColumn(int failures) {
      successes += headerRows;
      successes += rowCount - failures;
      this.failures += failures;
      return this;
    }

    public CollectionVerifier withColumn(int failures, int ignoreds) {
      successes += headerRows;
      successes += rowCount - failures - ignoreds;
      this.failures += failures;
      this.ignoreds += ignoreds;
      return this;
    }

    public CollectionVerifier withFailingColumn() {
      this.failures += headerRows;
      this.ignoreds += rowCount;
      return this;
    }

    public int getSuccesses() {
      return successes;
    }

    public int getFailures() {
      return failures;
    }

    public int getIgnoreds() {
      return ignoreds;
    }

  }
}
