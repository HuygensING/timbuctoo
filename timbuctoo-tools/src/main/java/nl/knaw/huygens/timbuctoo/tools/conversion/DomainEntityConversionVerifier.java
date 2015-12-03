package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
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

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

public class DomainEntityConversionVerifier<T extends DomainEntity> extends AbstractEntityConversionVerifier<T> {

  protected final int revision;

  public DomainEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, int revision) {
    this(type, mongoStorage, graphStorage, new PropertyVerifier(), revision);
  }

  DomainEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, PropertyVerifier propertyVerifier, int revision) {
    super(type, mongoStorage, graphStorage, propertyVerifier);
    this.revision = revision;
  }

  @Override
  protected T getOldItem(String oldId) throws StorageException {
    T revEntity = mongoStorage.getRevision(type, oldId, revision);

    return revEntity != null ? revEntity : mongoStorage.getEntity(type, oldId);
  }
}
