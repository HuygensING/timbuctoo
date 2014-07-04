package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface IndexManager {

  <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException;

  <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException;

  <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException;

  <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException;

  void deleteAllEntities() throws IndexException;

  IndexStatus getStatus();

  void commitAll() throws IndexException;

  void close();

}
