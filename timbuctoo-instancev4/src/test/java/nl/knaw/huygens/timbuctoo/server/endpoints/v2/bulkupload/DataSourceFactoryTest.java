package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaResource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import java.io.StringReader;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DataSourceFactoryTest {

  @Test
  public void parsesTimbuctooDataSource() {
    RdfResource wrappedResource = parseRdf("{\n" +
      "  \"@id\": \"http://example.com/target\"," +
      "  \"http://semweb.mmlab.be/ns/rml#source\": {\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#rawCollection\": \"CollectionName\",\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#vreName\": \"VreName\"\n" +
      "  }\n" +
      "}\n");

    DataSourceFactory dataSourceFactory = new DataSourceFactory(newGraph().wrap());

    DataSource result = dataSourceFactory.apply(wrappedResource)
      .orElseThrow(() -> new AssertionError("No result found"));

    assertThat(result.toString(), is("    BulkUploadedDatasource: VreName, CollectionName\n"));
  }

  @Test
  public void parsesExpressions() {
    RdfResource wrappedResource = parseRdf("{\n" +
      "  \"@id\": \"http://example.com/target\",\n" +
      "  \"http://semweb.mmlab.be/ns/rml#source\": {\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#rawCollection\": \"CollectionName\",\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#vreName\": \"VreName\",\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#customField\": [\n" +
      "      {\n" +
      "        \"http://timbuctoo.huygens.knaw.nl/mapping#name\": \"expr\",\n" +
      "        \"http://timbuctoo.huygens.knaw.nl/mapping#expression\": \"v.someColumn + 2\"\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}\n");

    DataSourceFactory dataSourceFactory = new DataSourceFactory(newGraph().wrap());

    DataSource result = dataSourceFactory.apply(wrappedResource)
      .orElseThrow(() -> new AssertionError("No result found"));

    assertThat(result.toString(), is(
      "    BulkUploadedDatasource: VreName, CollectionName\n" +
      "      expr: v.someColumn + 2\n"
    ));
  }

  private RdfResource parseRdf(String timbuctooDataSource) {
    final Model model = ModelFactory.createDefaultModel();
    model.read(new ReaderInputStream(new StringReader(timbuctooDataSource)), null, "JSON-LD");

    Resource dataSourceDescription = model.getResource("http://example.com/target");
    return JenaResource.fromModel(model, dataSourceDescription);
  }

}
