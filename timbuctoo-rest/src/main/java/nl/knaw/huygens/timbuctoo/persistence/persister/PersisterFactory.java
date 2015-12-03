package nl.knaw.huygens.timbuctoo.persistence.persister;

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
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.persistence.Persister;

import javax.inject.Inject;

public class PersisterFactory {
  private final Repository repository;
  private final PersistenceWrapper persistenceWrapper;

  @Inject
  public PersisterFactory(Repository repository, PersistenceWrapper persistenceWrapper) {
    this.repository = repository;
    this.persistenceWrapper = persistenceWrapper;
  }

  public Persister forActionType(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddPersister(repository, persistenceWrapper);
      case MOD:
        return new ModPersister(persistenceWrapper);
      default:
        return new NoOpPersister();
    }
  }
}
