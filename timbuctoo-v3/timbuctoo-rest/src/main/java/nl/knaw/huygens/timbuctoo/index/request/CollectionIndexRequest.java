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
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class CollectionIndexRequest extends AbstractIndexRequest {
  private final Repository repository;

  public CollectionIndexRequest(IndexerFactory indexerFactory, ActionType actionType, Class<? extends DomainEntity> type, Repository repository) {
    super(indexerFactory, actionType, type);
    this.repository = repository;
  }

  @Override
  public void execute() throws IndexException {
    Indexer indexer = getIndexerFactory().create(this.getActionType());
    Class<? extends DomainEntity> type = getType();

    StorageIterator<? extends DomainEntity> entities = repository.getDomainEntities(type);

    for (; entities.hasNext(); ) {
      indexer.executeIndexAction(type, entities.next().getId());
    }
  }

  @Override
  public Action toAction() {
    return new Action(getActionType(), getType());
  }
}
