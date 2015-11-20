package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

class RelationIndexRequest extends EntityIndexRequest{

  private final Repository repository;
  private final TypeRegistry typeRegistry;

  public RelationIndexRequest(IndexerFactory indexerFactory, Repository repository, TypeRegistry typeRegistry, ActionType actionType, Class<? extends DomainEntity> type, String id) {
    super(indexerFactory, actionType, type, id);
    this.repository = repository;
    this.typeRegistry = typeRegistry;
  }

  @Override
  public void execute() throws IndexException {
    Indexer relationIndexer = getIndexerFactory().create(getActionType());

    relationIndexer.executeIndexAction(getType(), getId());

    Relation relation = repository.getEntityOrDefaultVariation(Relation.class, getId());

    Indexer sourceTargetIndexer = getIndexerFactory().create(ActionType.MOD);

    Class<? extends DomainEntity> sourceType = typeRegistry.getDomainEntityType(relation.getSourceType());
    sourceTargetIndexer.executeIndexAction(sourceType, relation.getSourceId());

    Class<? extends DomainEntity> targetType = typeRegistry.getDomainEntityType(relation.getTargetType());
    sourceTargetIndexer.executeIndexAction(targetType, relation.getTargetId());

  }

}
