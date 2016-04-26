package nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexerSortFieldDescription;
import nl.knaw.huygens.timbuctoo.server.healthchecks.CompositeValidationResult;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ElementValidationResult;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class SortIndexesDatabaseCheck implements DatabaseCheck {

  protected ValidationResult getValidationResultForType(Vertex vertex, IndexDescription indexDescription) {
    List<IndexerSortFieldDescription> sortFieldDescriptions = indexDescription.getSortFieldDescriptions();


    List<String> expectedSortFields = sortFieldDescriptions.stream()
            .map(IndexerSortFieldDescription::getSortPropertyName)
            .collect(toList());


    for (IndexerSortFieldDescription expectedSortField : sortFieldDescriptions) {
      final String expectedSortProperty = expectedSortField.getSortPropertyName();

      VertexProperty<Object> vertexProperty = vertex.property(expectedSortProperty);
      if (!vertexProperty.isPresent() ||
              vertexProperty.value() == null) {

        String message = String.format("Vertex with tim_id %s misses field %s. Expected fields: %s",
                getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"),
                expectedSortProperty, expectedSortFields);

        return new ElementValidationResult(false, message);
      } else if (!vertexProperty.value().getClass().isAssignableFrom(expectedSortField.getType()) ) {
        String message = String.format("Vertex with tim_id %s has incorrect data type for property %s\n" +
                        "Expected: %s, got: %s",
                getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"),
                expectedSortProperty,
                expectedSortField.getType(),
                vertexProperty.value().getClass().getName());

        return new ElementValidationResult(false, message);
      }
    }


    return new ElementValidationResult(true,
            String.format("Vertex with tim_id %s is valid.",
                    getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"))
    );
  }

  @Override
  public ValidationResult check(Vertex vertex) {
    List<String> types = Arrays.asList(getEntityTypes(vertex)
            .orElseGet(() -> Try.success(new String[0]))
            .getOrElse(() -> new String[0]));

    List<IndexDescription> indexDescriptions = new IndexDescriptionFactory().getIndexersForTypes(types);

    List<ValidationResult> results = indexDescriptions.stream()
            .map(indexDescription -> getValidationResultForType(vertex, indexDescription))
            .collect(Collectors.toList());

    return new CompositeValidationResult(results);
  }
}
