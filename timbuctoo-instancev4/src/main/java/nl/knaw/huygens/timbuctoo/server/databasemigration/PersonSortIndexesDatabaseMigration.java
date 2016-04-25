package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.google.common.collect.Sets;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;

public class PersonSortIndexesDatabaseMigration implements DatabaseMigration {
  public static final Logger LOG = LoggerFactory.getLogger(PersonSortIndexesDatabaseMigration.class);

  Set<String> propertyFieldNames = Sets.newHashSet();

  @Override
  public String getName() {
    return this.getClass().getName();
  }



  @Override
  public void generateIndexes(Neo4jGraph neo4jGraph, Transaction transaction) {
    executeCypher(neo4jGraph, "person", "person_deathDate_sort", transaction);
    executeCypher(neo4jGraph, "person", "person_names_sort", transaction);
    executeCypher(neo4jGraph, "person", "person_birthDate_sort", transaction);

    executeCypher(neo4jGraph, "wwperson", "wwperson_birthDate_sort", transaction);
    executeCypher(neo4jGraph, "wwperson", "wwperson_names_sort", transaction);
    executeCypher(neo4jGraph, "wwperson", "wwperson_deathDate_sort", transaction);

    executeCypher(neo4jGraph, "ckccperson", "ckccperson_deathDate_sort", transaction);
    executeCypher(neo4jGraph, "ckccperson", "ckccperson_names_sort", transaction);
    executeCypher(neo4jGraph, "ckccperson", "ckccperson_birthDate_sort", transaction);

    executeCypher(neo4jGraph, "dcarperson", "dcarperson_deathDate_sort", transaction);
    executeCypher(neo4jGraph, "dcarperson", "dcarperson_birthDate_sort", transaction);
    executeCypher(neo4jGraph, "dcarperson", "dcarperson_names_sort", transaction);

    executeCypher(neo4jGraph, "cwnoperson", "cwnoperson_deathDate_sort", transaction);
    executeCypher(neo4jGraph, "cwnoperson", "cwnoperson_names_sort", transaction);
    executeCypher(neo4jGraph, "cwnoperson", "cwnoperson_birthDate_sort", transaction);

    executeCypher(neo4jGraph, "cwrsperson", "cwrsperson_deathDate_sort", transaction);
    executeCypher(neo4jGraph, "cwrsperson", "cwrsperson_names_sort", transaction);
    executeCypher(neo4jGraph, "cwrsperson", "cwrsperson_birthDate_sort", transaction);

    executeCypher(neo4jGraph, "person", "modified_sort", transaction);
    executeCypher(neo4jGraph, "wwperson", "modified_sort", transaction);
    executeCypher(neo4jGraph, "ckccperson", "modified_sort", transaction);
    executeCypher(neo4jGraph, "dcarperson", "modified_sort", transaction);
    executeCypher(neo4jGraph, "cwnoperson", "modified_sort", transaction);
    executeCypher(neo4jGraph, "cwrsperson", "modified_sort", transaction);
  }

  private void executeCypher(Neo4jGraph neo4jGraph, String label, String propertyName,Transaction transaction) {
    String cypherQuery = String.format("CREATE INDEX ON :%s(%s)", label, propertyName);
    LOG.info(cypherQuery);
    transaction.open();
    neo4jGraph.cypher(cypherQuery);
    transaction.commit();
    transaction.close();
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    List<String> types = Arrays.asList(getEntityTypes(vertex)
            .orElseGet(() -> Try.success(new String[0]))
            .getOrElse(() -> new String[0]));

    if (types.contains("person")) {
      IndexDescription indexDescription = new IndexDescriptionFactory().create(types).get();
      indexDescription.addIndexedSortProperties(vertex);
      propertyFieldNames.addAll(indexDescription.getSortIndexPropertyNames());
    }
  }
}
