package nl.knaw.huygens.timbuctoo.persistence;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class PersistenceWrapper {

  private final PersistenceManager manager;
  private final String baseUrl;
  private final TypeRegistry typeRegistry;

  @Inject
  public PersistenceWrapper(String baseUrl, PersistenceManager persistenceManager, TypeRegistry typeRegistry) {
    this.baseUrl = CharMatcher.is('/').trimTrailingFrom(baseUrl);
    this.manager = persistenceManager;
    Preconditions.checkNotNull(this.manager);
    this.typeRegistry = typeRegistry;
  }

  public String persistURL(String url) throws PersistenceException {
    return manager.persistURL(url);
  }

  public String getURLValue(String persistentId) throws PersistenceException {
    return manager.getPersistedURL(persistentId);
  }

  public String getPersistentURL(String persistentId) {
    return manager.getPersistentURL(persistentId);
  }

  public String persistObject(Class<? extends Entity> type, String objectId) throws PersistenceException {
    String url = createURL(type, objectId);
    return manager.persistURL(url);
  }

  public String persistObject(Class<? extends Entity> type, String objectId, int revision) throws PersistenceException {
    String url = createURL(type, objectId, revision);
    return manager.persistURL(url);
  }

  public void deletePersistentId(String persistentId) throws PersistenceException {
    manager.deletePersistentId(persistentId);
  }

  private String createURL(Class<? extends Entity> type, String id) {
    String collection = typeRegistry.getXNameForType(type);
    return Joiner.on('/').join(baseUrl, Paths.DOMAIN_PREFIX, collection, id);
  }

  private String createURL(Class<? extends Entity> type, String id, int revision) {
    return createURL(type, id) + "?rev=" + revision;
  }
}
