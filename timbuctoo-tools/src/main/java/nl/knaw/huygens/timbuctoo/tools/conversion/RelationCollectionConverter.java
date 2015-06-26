package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;

public class RelationCollectionConverter {

  private RelationConverter versionConverter;
  private MongoConversionStorage mongoStorage;

  public RelationCollectionConverter(MongoConversionStorage mongoStorage, Graph graph, GraphStorage graphStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap, IdGenerator idGenerator) {
    this(new RelationConverter(mongoStorage, graph, graphStorage, typeRegistry, oldIdNewIdMap, idGenerator), mongoStorage);
  }

  RelationCollectionConverter(RelationConverter versionConverter, MongoConversionStorage mongoStorage) {
    this.versionConverter = versionConverter;
    this.mongoStorage = mongoStorage;

  }

  public void convert() throws StorageException, IllegalArgumentException, IllegalAccessException {
    List<String> relationIds = Lists.newArrayList();

    //first create the jobs to prevent a mongo cursor timeout exception.
    for (StorageIterator<Relation> relations = mongoStorage.getDomainEntities(Relation.class); relations.hasNext();) {
      relationIds.add(relations.next().getId());
    }

    for (String id : relationIds) {
      versionConverter.convert(id);
    }

  }
}
