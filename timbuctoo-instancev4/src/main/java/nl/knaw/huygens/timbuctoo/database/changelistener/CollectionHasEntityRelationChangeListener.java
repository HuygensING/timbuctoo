package nl.knaw.huygens.timbuctoo.database.changelistener;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class CollectionHasEntityRelationChangeListener implements ChangeListener {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CollectionHasEntityRelationChangeListener.class);

  private final GraphWrapper graphWrapper;

  public CollectionHasEntityRelationChangeListener(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @Override
  public void onCreate(Collection collection, Vertex vertex) {
    String[] types = getEntityTypesOrDefault(vertex);

    addCollectionRelations(vertex, Sets.newHashSet(types));
  }


  @Override
  public void onPropertyUpdate(Collection collection, Optional<Vertex> ignored, Vertex vertexToUpdate) {
    final Set<String> desiredEntityTypes = Sets.newHashSet(getEntityTypesOrDefault(vertexToUpdate));
    final Set<String> currentEntityTypes = graphWrapper
      .getGraph().traversal().V(vertexToUpdate)
      .in(Collection.HAS_ENTITY_RELATION_NAME)
      .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
      .map(vertexTraverser -> (String) vertexTraverser.get().value(
        Collection.ENTITY_TYPE_NAME_PROPERTY_NAME))
      .toSet();

    Set<String> entityTypesToAdd = Sets.difference(desiredEntityTypes, currentEntityTypes);
    Set<String> entityTypesToRemove = Sets.difference(currentEntityTypes, desiredEntityTypes)
                                          .stream()
                                          .filter(type -> !type.equals("vertex"))
                                          .collect(Collectors.toSet());

    addCollectionRelations(vertexToUpdate, entityTypesToAdd);

    for (String type : entityTypesToRemove) {
      final GraphTraversal<Vertex, Vertex> hasEntityNode = findEntityNodeForEntityTypeName(type);
      final GraphTraversal<Vertex, Edge> edgeToRemove = hasEntityNode.outE(Collection.HAS_ENTITY_RELATION_NAME)
                                                                     .where(__.inV().is(vertexToUpdate));
      if (edgeToRemove.hasNext()) {
        edgeToRemove.next().remove();
      }
    }
  }

  @Override
  public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    onPropertyUpdate(collection, oldVertex, newVertex);
  }

  @Override
  public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

  }

  private void addCollectionRelations(Vertex vertex, Set<String> types) {
    for (String type : types) {
      final GraphTraversal<Vertex, Vertex> hasEntityNode = findEntityNodeForEntityTypeName(type);

      if (hasEntityNode.hasNext()) {
        hasEntityNode.next().addEdge(Collection.HAS_ENTITY_RELATION_NAME, vertex);
      } else {
        LOG.error(databaseInvariant, "No hasEntity node found for collection with entityTypeName {} ", type);
      }
    }
  }

  private GraphTraversal<Vertex, Vertex> findEntityNodeForEntityTypeName(String type) {
    return graphWrapper
      .getGraph().traversal().V()
      .hasLabel(Collection.DATABASE_LABEL)
      .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, type)
      .out(Collection.HAS_ENTITY_NODE_RELATION_NAME);
  }
}
