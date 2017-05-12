package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.NS_RML;

public class DataSourceDescriptionParser {
  public static Optional<DataSourceDescription> getDataSourceDescription(RdfResource rdfResource) {
    Optional<DataSourceDescription> description = Optional.empty();
    for (RdfResource resource : rdfResource.out(NS_RML + "source")) {
      Set<RdfResource> rawCollection = resource.out("http://timbuctoo.huygens.knaw.nl/mapping#rawCollection");
      Set<RdfResource> customFields = resource.out("http://timbuctoo.huygens.knaw.nl/mapping#customField");

      Map<String, String> expressions = new HashMap<>();
      for (RdfResource customField : customFields) {
        Set<RdfResource> fieldNameResource = customField.out("http://timbuctoo.huygens.knaw.nl/mapping#name");
        Set<RdfResource> fieldValueResource = customField.out("http://timbuctoo.huygens.knaw.nl/mapping#expression");
        fieldNameResource.iterator().next().asLiteral().ifPresent(fieldName -> {
          fieldValueResource.iterator().next().asLiteral().ifPresent(fieldValue -> {
            expressions.put(fieldName.getValue(), fieldValue.getValue());
          });
        });
      }


      if (rawCollection.size() == 1) {
        RdfResource collectionName = rawCollection.iterator().next();
        String value = collectionName.asLiteral().map(l -> l.getValue()).orElseGet(() -> collectionName.asIri().get());
        description = Optional.of(new DataSourceDescription(value, expressions));
        break;
      }
    }
    return description;
  }
}
