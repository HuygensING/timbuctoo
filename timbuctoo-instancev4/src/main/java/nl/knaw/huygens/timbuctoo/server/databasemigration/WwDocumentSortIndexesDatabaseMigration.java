package nl.knaw.huygens.timbuctoo.server.databasemigration;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;

public class WwDocumentSortIndexesDatabaseMigration extends AbstractVertexMigration {
  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    List<String> types = Arrays.asList(getEntityTypes(vertex)
      .orElseGet(() -> Try.success(new String[0]))
      .getOrElse(() -> new String[0]));

    if (types.contains("wwdocument")) {
      IndexDescription indexDescription = new IndexDescriptionFactory().create("wwdocument");
      indexDescription.addIndexedSortProperties(vertex);
    }
  }
}
