package nl.knaw.huygens.timbuctoo.model.neww;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.util.DefaultRelationRefCreator;

import com.google.inject.Inject;

public class WWRelationRefCreator extends DefaultRelationRefCreator {

  @Inject
  public WWRelationRefCreator(TypeRegistry typeRegistry, Storage storage) {
    super(typeRegistry, storage);
  }

  @Override
  protected RelationRef createRef(Relation relation, String refId, String relationName, Class<? extends DomainEntity> refType, DomainEntity refEntity) {
    if (relation instanceof WWRelation) {
      return new WWRelationRef(TypeNames.getInternalName(refType), TypeNames.getExternalName(refType), refId, refEntity.getIdentificationName(), relation.getId(), relation.isAccepted(),
          relation.getRev(), relationName, ((WWRelation) relation).getDate());
    }

    return super.createRef(relation, refId, relationName, refType, refEntity);

  }

}
