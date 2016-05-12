package nl.knaw.huygens.timbuctoo.crud.changelistener;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.crud.ChangeListener;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class FulltextIndexChangeListener implements ChangeListener {

  private final GraphDatabaseService graphDatabase;
  private final IndexDescriptionFactory indexDescriptionFactory;

  public FulltextIndexChangeListener(GraphDatabaseService graphDatabase,
                                     IndexDescriptionFactory indexDescriptionFactory) {
    this.graphDatabase = graphDatabase;
    this.indexDescriptionFactory = indexDescriptionFactory;
  }

  @Override
  public void onCreate(Vertex vertex) {
    handleChange(vertex);
  }

  @Override
  public void onUpdate(Optional<Vertex> oldVertex, Vertex newVertex) {
    Set<String> newTypes = Sets.newHashSet(getEntityTypesOrDefault(newVertex));
    Set<String> oldTypes = oldVertex.isPresent() ?
            Sets.newHashSet(getEntityTypesOrDefault(oldVertex.get())) :
            Sets.newHashSet(getEntityTypesOrDefault(newVertex));

    Set<String> typesToRemoveFromIndex = Sets.difference(oldTypes, newTypes);

    if (typesToRemoveFromIndex.size() > 0) {
      handleRemove(oldVertex.get(), Lists.newArrayList(typesToRemoveFromIndex));
    }
    handleChange(newVertex);
  }

  private void handleChange(Vertex vertex) {
    List<String> types = Lists.newArrayList(getEntityTypesOrDefault(vertex));
    List<IndexDescription> indexers = indexDescriptionFactory.getIndexersForTypes(types);
    for (IndexDescription indexer : indexers) {
      indexer.addToFulltextIndex(vertex, graphDatabase);
    }
  }

  private void handleRemove(Vertex vertex, List<String> types) {
    List<IndexDescription> indexers = indexDescriptionFactory.getIndexersForTypes(types);
    for (IndexDescription indexer : indexers) {
      indexer.removeFromFulltextIndex(vertex, graphDatabase);
    }
  }
}
