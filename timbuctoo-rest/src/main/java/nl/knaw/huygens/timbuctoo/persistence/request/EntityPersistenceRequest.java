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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;

class EntityPersistenceRequest implements PersistenceRequest {
  private final Repository repository;
  private final PersisterFactory persisterFactory;
  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;
  private final String id;

  public EntityPersistenceRequest(Repository repository, PersisterFactory persisterFactory, ActionType actionType, Class<? extends DomainEntity> type, String id) {
    this.repository = repository;
    this.persisterFactory = persisterFactory;
    this.actionType = actionType;
    this.type = type;
    this.id = id;
  }

  @Override
  public Action toAction() {
    return new Action(actionType, type, id);
  }

  @Override
  public void execute() {
    Persister persister = persisterFactory.forActionType(actionType);
    DomainEntity domainEntity = repository.getEntityOrDefaultVariation(type, id);
    persister.execute(domainEntity);
  }
}
