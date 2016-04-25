package nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexerSortFieldDescription;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ElementValidationResult;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class WwDocumentSortIndexesDatabaseCheck extends SortIndexesDatabaseCheck {


  @Override
  public ValidationResult check(Vertex vertex) {

    return getValidationResultForType(vertex, "wwdocument");
  }
}
