package nl.knaw.huygens.timbuctoo.search.description;


import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexerSortFieldDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import java.util.List;
import java.util.Set;

public interface IndexDescription {

  List<IndexerSortFieldDescription> getSortFieldDescriptions();

  void addIndexedSortProperties(Vertex vertex);

  void addToFulltextIndex(Vertex vertex, Index<Node> index);
}
