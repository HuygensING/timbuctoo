package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

public class RelationConverter {

  private static final Class<Relation> TYPE = Relation.class;
  private final MongoConversionStorage mongoStorage;
  private final RelationRevisionConverter revisionConverter;
  private final IdGenerator idGenerator;

  public RelationConverter(MongoConversionStorage mongoStorage, RelationRevisionConverter revisionConverter, IdGenerator idGenerator) {
    this.mongoStorage = mongoStorage;
    this.revisionConverter = revisionConverter;
    this.idGenerator = idGenerator;
  }

  public void convert(String oldId) throws StorageException {
    AllVersionVariationMap<Relation> map = mongoStorage.getAllVersionVariationsMapOf(TYPE, oldId);
    String newId = idGenerator.nextIdFor(TYPE);
    for (int revision : map.revisionsInOrder()) {
      revisionConverter.convert(oldId, newId, map.get(revision));
    }
  }
}
