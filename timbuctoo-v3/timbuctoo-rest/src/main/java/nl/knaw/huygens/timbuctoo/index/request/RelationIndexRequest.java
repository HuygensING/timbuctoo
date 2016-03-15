package nl.knaw.huygens.timbuctoo.index.request;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
