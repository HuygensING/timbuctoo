package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

class Neo4JStorageIteratorFactory {

  private PropertyContainerConverterFactory propertyContainerConverterFactory;

  public Neo4JStorageIteratorFactory(PropertyContainerConverterFactory propertyContainerConverterFactory) {
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
  }

  public <T extends Entity> StorageIterator<T> forNode(Class<T> type, Iterable<Node> nodes) throws StorageException {
    NodeConverter<T> nodeConverter = propertyContainerConverterFactory.createForType(type);

    List<T> entities = Lists.newArrayList();

    for (Node node : nodes) {
      try {
        entities.add(nodeConverter.convertToEntity(node));
      } catch (InstantiationException e) {
        throw new StorageException(e);
      }
    }

    return StorageIteratorStub.newInstance(entities);
  }

  public <T extends Relation> StorageIterator<T> forRelationship(Class<T> relationType, List<Relationship> relationships) throws StorageException {
    RelationshipConverter<T> relationshipConverter = propertyContainerConverterFactory.createForRelation(relationType);
    List<T> relations = Lists.newArrayList();

    for (Relationship relationship : relationships) {
      try {
        relations.add(relationshipConverter.convertToEntity(relationship));
      } catch (InstantiationException e) {
        throw new StorageException(e);
      }
    }

    return StorageIteratorStub.newInstance(relations);
  }

}
