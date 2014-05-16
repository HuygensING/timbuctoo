package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo core
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
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

/**
 * Defines a subset of the data in the repository.
 */
public interface Scope {

  /**
   * Returns primitive domain entity types in this scope.
   */
  Set<Class<? extends DomainEntity>> getBaseEntityTypes();

  /**
   * Checks if the {@code type} is in scope. A light method to check if it is needed to do a further check.
   * @param type the type to check if it is in scope.
   * @return {@code true} if the type is in scope {@code false} if not.
   */
  <T extends DomainEntity> boolean inScope(Class<T> type);

  /**
   * Returns {@code true} if the specified domain entity
   * is in scope, {@code false} otherwise.
   */
  <T extends DomainEntity> boolean inScope(Class<T> type, String id);

  /**
   * Returns {@code true} if the specified domain entity
   * is in scope, {@code false} otherwise.
   */
  <T extends DomainEntity> boolean inScope(T entity);

  /**
   * Checks if the {@code type} is in scope. A light method to check if it is needed to do a further check.
   * @param type the type to check if it is in scope.
   * @return {@code true} if the type is in scope {@code false} if not.
   */
  <T extends DomainEntity> boolean inScope(Class<T> type);

  /**
   * Filters a list of entities to return the a list of entities that are in scope. 
   * @param entities the list to filter.
   * @return the entities in scope.
   */
  <T extends DomainEntity> List<T> filter(final List<T> entities);

}
