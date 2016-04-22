package nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks;

import com.google.common.collect.Lists;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ElementValidationResult;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class PersonSortIndexesDatabaseCheck implements DatabaseCheck {

  @Override
  public ValidationResult check(Vertex vertex) {
    List<String> types = Arrays.asList(getEntityTypes(vertex)
            .orElseGet(() -> Try.success(new String[0]))
            .getOrElse(() -> new String[0]));

    if (types.contains("person")) {
      for (String type : types) {
        List<String> expectedSortFields = Lists.newArrayList(
                String.format("%s_names_sort", type),
                String.format("%s_deathDate_sort", type),
                String.format("%s_birthDate_sort", type),
                String.format("%s_modified_sort", type)
        );

        for (String expectedSortField : expectedSortFields) {
          if (!vertex.property(expectedSortField).isPresent() || vertex.property(expectedSortField).value() == null) {
            String message = String.format("Vertex with tim_id %s misses field %s. Expected fields: %s",
                    getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"), expectedSortField, expectedSortFields);

            return new ElementValidationResult(false, message);
          }
        }
      }
    }

    return new ElementValidationResult(true,
            String.format("Vertex with tim_id %s is valid.",
                    getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"))
    );
  }
}
