package nl.knaw.huygens.timbuctoo.search.description;


import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexerSortFieldDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;

public interface IndexDescription {

  List<IndexerSortFieldDescription> getSortFieldDescriptions();

  void addIndexedSortProperties(Vertex vertex);

  void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase);

  void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase);
}
