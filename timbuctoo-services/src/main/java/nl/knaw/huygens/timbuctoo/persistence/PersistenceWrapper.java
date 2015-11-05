package nl.knaw.huygens.timbuctoo.persistence;

/*
 * #%L
 * Timbuctoo services
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

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PersistenceWrapper {

  private final PersistenceManager manager;
  private final String baseUrl;
  private static final Logger LOG = LoggerFactory.getLogger(PersistenceWrapper.class);

  @Inject
  public PersistenceWrapper(@Named("public_url") String baseUrl, PersistenceManager persistenceManager) {
    LOG.info("base url: {}", baseUrl);
    Preconditions.checkArgument(!StringUtils.isEmpty(baseUrl), "baseUrl cannot be empty");
    this.baseUrl = CharMatcher.is('/').trimTrailingFrom(baseUrl);
    this.manager = persistenceManager;
    Preconditions.checkNotNull(this.manager);
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
    String collection = getCollectionName(type);
    return Joiner.on('/').join(baseUrl, getPrefix(type), collection, id);
  }

  private String getPrefix(Class<? extends Entity> type) {
    return TypeRegistry.isDomainEntity(type) ? Paths.DOMAIN_PREFIX : Paths.SYSTEM_PREFIX;
  }

  private String getCollectionName(Class<? extends Entity> type) {
    Class<? extends Entity> typeForCollectionName = type;
    if (TypeRegistry.isDomainEntity(type)) {
      typeForCollectionName = TypeRegistry.toBaseDomainEntity(TypeRegistry.toDomainEntity(type));
    }

    return TypeNames.getExternalName(typeForCollectionName);
  }

  private String createURL(Class<? extends Entity> type, String id, int revision) {
    return createURL(type, id) + "?rev=" + revision;
  }

  public void updatePID(DomainEntity domainEntity) throws PersistenceException{
    String url = createURL(domainEntity.getClass(), domainEntity.getId(), domainEntity.getRev());

    manager.modifyURLForPersistentId(getPID(domainEntity), url);

  }

  private String getPID(DomainEntity entity) {
    if (entity.getPid() == null) {
      return null;
    }

    String[] splittedPIDURI = entity.getPid().split("/");

    return splittedPIDURI[splittedPIDURI.length - 1];
  }
}
