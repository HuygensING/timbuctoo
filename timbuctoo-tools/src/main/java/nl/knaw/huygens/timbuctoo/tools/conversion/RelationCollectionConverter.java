package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import com.google.common.collect.Lists;

public class RelationCollectionConverter {

  private RelationVersionConverter versionConverter;
  private MongoConversionStorage mongoStorage;

  public RelationCollectionConverter(RelationVersionConverter versionConverter, MongoConversionStorage mongoStorage) {
    this.versionConverter = versionConverter;
    this.mongoStorage = mongoStorage;

  }

  public void convert() throws StorageException {
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
