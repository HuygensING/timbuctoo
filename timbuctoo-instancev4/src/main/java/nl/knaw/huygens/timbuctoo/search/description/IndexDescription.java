package nl.knaw.huygens.timbuctoo.search.description;


import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexerSortFieldDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Set;

public interface IndexDescription {

  List<IndexerSortFieldDescription> getSortFieldDescriptions();

  void addIndexedSortProperties(Vertex vertex);
}
