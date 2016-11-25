package nl.knaw.huygens.timbuctoo.database.changelistener;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class DenormalizedSortFieldUpdater implements ChangeListener {
  private IndexDescriptionFactory indexDescriptionFactory;

  public DenormalizedSortFieldUpdater(IndexDescriptionFactory indexDescriptionFactory) {
    this.indexDescriptionFactory = indexDescriptionFactory;
  }

  @Override
  public void onCreate(Vertex vertex) {
    handleChange(vertex);
  }

  @Override
  public void onUpdate(Optional<Vertex> oldVertex, Vertex newVertex) {
    handleChange(newVertex);
  }

  private void handleChange(Vertex vertex) {
    List<String> types = Lists.newArrayList(getEntityTypesOrDefault(vertex));
    List<IndexDescription> indexers = indexDescriptionFactory.getIndexersForTypes(types);
    for ( IndexDescription indexer : indexers) {
      indexer.addIndexedSortProperties(vertex);
    }
  }
}
