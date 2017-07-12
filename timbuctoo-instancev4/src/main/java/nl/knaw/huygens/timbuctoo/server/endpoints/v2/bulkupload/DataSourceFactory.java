package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DataSourceFactory {
  private final GraphWrapper graphWrapper;
  private static final String NS_RML = "http://semweb.mmlab.be/ns/rml#";

  public DataSourceFactory(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public Optional<DataSource> apply(RdfResource rdfResource, String vreName) {
    for (RdfResource resource : rdfResource.out(NS_RML + "source")) {
      Set<RdfResource> rawCollection = resource.out("http://timbuctoo.huygens.knaw.nl/mapping#rawCollection");
      Set<RdfResource> ognlFieldResources = resource.out("http://timbuctoo.huygens.knaw.nl/mapping#customField");

      Map<String, String> expressions = new HashMap<>();
      for (RdfResource ognlField : ognlFieldResources) {
        Set<RdfResource> fieldNameResource = ognlField.out("http://timbuctoo.huygens.knaw.nl/mapping#name");
        Set<RdfResource> fieldValueResource = ognlField.out("http://timbuctoo.huygens.knaw.nl/mapping#expression");
        fieldNameResource.iterator().next().asLiteral().ifPresent(fieldName -> {
          fieldValueResource.iterator().next().asLiteral().ifPresent(fieldValue -> {
            expressions.put(fieldName.getValue(), fieldValue.getValue());
          });
        });
      }


      if (rawCollection.size() == 1) {
        return Optional.of(new BulkUploadedDataSource(
          vreName,
          rawCollection.iterator().next().asLiteral().get().getValue(),
          graphWrapper,
          new JexlRowFactory(expressions, new HashMapBasedJoinHandler())
        ));
      }
    }
    return Optional.empty();
  }
}
