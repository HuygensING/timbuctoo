package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.jena.JenaResource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import java.io.StringReader;

import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.DataSourceDescriptionParser.getDataSourceDescription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DataSourceFactoryTest {

  @Test
  public void parsesTimbuctooDataSource() {
    RdfResource wrappedResource = parseRdf("{\n" +
      "  \"@id\": \"http://example.com/target\"," +
      "  \"http://semweb.mmlab.be/ns/rml#source\": {\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#rawCollection\": \"CollectionName\"\n" +
      "  }\n" +
      "}\n");

    DataSourceDescription dataSourceDescription = getDataSourceDescription(wrappedResource).get();

    assertThat(dataSourceDescription.getCollection(), is("CollectionName"));
  }

  @Test
  public void parsesExpressions() {
    RdfResource wrappedResource = parseRdf("{\n" +
      "  \"@id\": \"http://example.com/target\",\n" +
      "  \"http://semweb.mmlab.be/ns/rml#source\": {\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#rawCollection\": \"CollectionName\",\n" +
      "    \"http://timbuctoo.huygens.knaw.nl/mapping#customField\": [\n" +
      "      {\n" +
      "        \"http://timbuctoo.huygens.knaw.nl/mapping#name\": \"expr\",\n" +
      "        \"http://timbuctoo.huygens.knaw.nl/mapping#expression\": \"v.someColumn + 2\"\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}\n");

    DataSourceDescription dataSourceDescription = getDataSourceDescription(wrappedResource).get();

    assertThat(dataSourceDescription.getCustomFields().get("expr"), is("v.someColumn + 2"));
  }

  private RdfResource parseRdf(String timbuctooDataSource) {
    final Model model = ModelFactory.createDefaultModel();
    model.read(new ReaderInputStream(new StringReader(timbuctooDataSource)), null, "JSON-LD");

    Resource dataSourceDescription = model.getResource("http://example.com/target");
    return JenaResource.fromModel(model, dataSourceDescription);
  }

}
