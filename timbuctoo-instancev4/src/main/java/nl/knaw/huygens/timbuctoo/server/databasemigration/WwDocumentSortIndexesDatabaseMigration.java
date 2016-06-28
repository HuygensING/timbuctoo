package nl.knaw.huygens.timbuctoo.server.databasemigration;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexerSortFieldDescription;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;

public class WwDocumentSortIndexesDatabaseMigration implements DatabaseMigration {
  public static final Logger LOG = LoggerFactory.getLogger(WwPersonSortIndexesDatabaseMigration.class);


  @Override
  public String getName() {
    return this.getClass().getName();
  }


  @Override
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    // before hook not needed
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    List<String> types = Arrays.asList(getEntityTypes(vertex)
            .orElseGet(() -> Try.success(new String[0]))
            .getOrElse(() -> new String[0]));

    if (types.contains("wwdocument")) {
      IndexDescription indexDescription = new IndexDescriptionFactory().create("wwdocument");
      indexDescription.addIndexedSortProperties(vertex);
      List<String> sortPropertyNames = indexDescription.getSortFieldDescriptions().stream()
              .map(IndexerSortFieldDescription::getSortPropertyName)
              .collect(toList());
    }
  }
}
