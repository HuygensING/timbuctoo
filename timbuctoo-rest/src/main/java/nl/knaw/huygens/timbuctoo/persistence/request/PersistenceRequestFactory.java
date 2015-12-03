package nl.knaw.huygens.timbuctoo.persistence.request;

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
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;

public class PersistenceRequestFactory {

  private Repository repository;
  private PersisterFactory persisterFactory;

  @Inject
  public PersistenceRequestFactory(Repository repository, PersisterFactory persisterFactory){
    this.repository = repository;
    this.persisterFactory = persisterFactory;
  }

  public PersistenceRequest forEntity(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    return new EntityPersistenceRequest(repository, persisterFactory, actionType, type, id);
  }

  public PersistenceRequest forCollection(ActionType actionType, Class<? extends DomainEntity> type) {
    return new CollectionPersistenceRequest(repository, persisterFactory, actionType, type);
  }

  public PersistenceRequest forAction(Action action) {
    if (action.isForMultiEntities()) {
      return forCollection(action.getActionType(), action.getType());
    }
    return forEntity(action.getActionType(), action.getType(), action.getId());
  }
}
