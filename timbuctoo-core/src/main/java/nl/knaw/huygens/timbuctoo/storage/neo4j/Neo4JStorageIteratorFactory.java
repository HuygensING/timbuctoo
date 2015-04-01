package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

class Neo4JStorageIteratorFactory {

  private PropertyContainerConverterFactory propertyContainerConverterFactory;

  public Neo4JStorageIteratorFactory(PropertyContainerConverterFactory propertyContainerConverterFactory) {
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
  }

  public <T extends Entity> StorageIterator<T> create(Class<T> type, Iterable<? extends T> iterable) {
    // FIXME Quick fix TIM-123
    return StorageIteratorStub.newInstance(Lists.newArrayList(iterable));
  }

  public <T extends Relation> StorageIterator<T> forRelation(Class<T> relationType, List<Relationship> relationships) throws StorageException {
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
