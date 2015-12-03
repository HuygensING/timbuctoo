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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class IndexRequestFactory {
  private final IndexerFactory indexerFactory;
  private final Repository repository;
  private final TypeRegistry typeRegistry;

  @Inject
  public IndexRequestFactory(IndexerFactory indexerFactory, Repository repository, TypeRegistry typeRegistry) {
    this.indexerFactory = indexerFactory;
    this.repository = repository;
    this.typeRegistry = typeRegistry;
  }

  public IndexRequest forCollectionOf(ActionType actionType, Class<? extends DomainEntity> type) {
    return new CollectionIndexRequest(indexerFactory, actionType, type, repository);
  }

  public IndexRequest forEntity(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    if (Relation.class.isAssignableFrom(type)) {
      return new RelationIndexRequest(indexerFactory, repository, typeRegistry, actionType, type, id);
    }
    return new EntityIndexRequest(indexerFactory, actionType, type, id);
  }

  public IndexRequest forAction(Action action) {
    if (action.isForMultiEntities()) {
      return forCollectionOf(action.getActionType(), action.getType());
    }

    return forEntity(action.getActionType(), action.getType(), action.getId());
  }
}
