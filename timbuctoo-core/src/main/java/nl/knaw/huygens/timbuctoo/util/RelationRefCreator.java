package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

public interface RelationRefCreator {

  RelationRef createRegular(EntityMapper mapper, Relation relation, RelationType relType) throws StorageException;

  RelationRef createInverse(EntityMapper mapper, Relation relation, RelationType relType) throws StorageException;

}