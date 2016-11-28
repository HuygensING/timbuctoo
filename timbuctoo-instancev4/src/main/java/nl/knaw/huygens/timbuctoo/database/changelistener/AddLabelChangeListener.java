package nl.knaw.huygens.timbuctoo.database.changelistener;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class AddLabelChangeListener implements ChangeListener {
  @Override
  public void onCreate(Collection collection, Vertex vertex) {
    String[] types = getEntityTypesOrDefault(vertex);

    for (String type :  types) {
      ((Neo4jVertex) vertex).addLabel(type);
    }
  }

  @Override
  public void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    Set<String> desiredLabels = Sets.newHashSet(getEntityTypesOrDefault(newVertex));
    Set<String> currentLabels = Sets.newHashSet(((Neo4jVertex) newVertex).labels());

    Set<String> labelsToAdd = Sets.difference(desiredLabels, currentLabels);
    Set<String> labelsToRemove = Sets.difference(currentLabels, desiredLabels).stream()
            .filter(type -> !type.equals("vertex")).collect(Collectors.toSet());

    labelsToAdd.forEach(((Neo4jVertex) newVertex)::addLabel);

    labelsToRemove.forEach(((Neo4jVertex) newVertex)::removeLabel);
  }

  @Override
  public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    onPropertyUpdate(collection, oldVertex, newVertex);
  }

  @Override
  public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

  }

  public void handleRdfLabelAdd(Vertex vertex, String entityTypeName) {
    ((Neo4jVertex) vertex).addLabel(entityTypeName);
  }

  public void handleRdfLabelRemove(Vertex vertex, String entityTypeName) {
    ((Neo4jVertex) vertex).removeLabel(entityTypeName);
  }

}
