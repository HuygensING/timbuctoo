package nl.knaw.huygens.timbuctoo.bulkupload;

import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkUploadServiceTest {

  private static Vre VRE = new Vres.Builder()
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
    ).build().getVre("EuropeseMigratie");

  @Test
  public void loadOneCollection() throws Exception {
    Graph graph = newGraph().build();
    final String filename = "basic_upload.xlsx";
    final BulkUploadService instance = new BulkUploadService(VRE, mockWrapper(graph));
    instance.saveToDb(getWorkbook(filename));
    final List<Vertex> vertices = graph.traversal().V().toList();
    assertThat(vertices.size(), is(5));
  }


  @Test
  public void withFailingValues() throws Exception {
    Graph graph = newGraph().build();
    final String filename = "failing_upload.xlsx";
    final BulkUploadService instance = new BulkUploadService(VRE, mockWrapper(graph));
    final XSSFWorkbook workbook = getWorkbook(filename);
    instance.saveToDb(workbook);
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
    final String filename = "basic_upload.xlsx";
    final BulkUploadService instance = new BulkUploadService(VRE, mockWrapper(graph));
    instance.saveToDb(getWorkbook(filename));
    instance.saveToDb(getWorkbook(filename));
    final List<Vertex> vertices = graph.traversal().V().toList();
    assertThat(vertices.size(), is(5));
  }

  @Test
  public void loadRelations() throws Exception {
    Graph graph = newGraph()
      .withVertex("relationType", v->v
        .withProperty("relationtype_regularName", "isStoredAt")
        .withProperty("relationtype_inverseName", "contains")
        .withProperty("relationtype_sourceTypeName", "document")
        .withProperty("relationtype_targetTypeName", "collective")
        .withProperty("tim_id", "id")
      )
      .build();
    final int vertexCountBeforeLoad = graph.traversal().V().toList().size();
    final String filename = "relations.xlsx";
    final BulkUploadService instance = new BulkUploadService(VRE, mockWrapper(graph));
    instance.saveToDb(getWorkbook(filename));
    final List<Vertex> vertices = graph.traversal().V().toList();
    assertThat(vertices.size(), is(7 + vertexCountBeforeLoad));
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
}
