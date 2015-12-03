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
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.EntityIds;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDB;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoQueries;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.DBObject;

public class MongoConversionStorage extends MongoStorage {

  private MongoQueries queries;

  @Inject
  public MongoConversionStorage(MongoDB mongoDB, EntityIds entityIds, Properties properties, EntityInducer inducer, EntityReducer reducer) {
    super(mongoDB, entityIds, properties, inducer, reducer);
    queries = new MongoQueries();
  }

  public <T extends DomainEntity> AllVersionVariationMap<T> getAllVersionVariationsMapOf(Class<T> type, String id) throws StorageException {
    DBObject selectById = queries.selectById(id);
    DBObject foundObject = getVersionCollection(type).findOne(selectById);

    if (foundObject != null) {
      JsonNode jsonNode = toJsonNode(foundObject);

      return AllVersionVariationMap.forVersionNode(type, jsonNode, reducer);
    } else {
      foundObject = getDBCollection(type).findOne(selectById);
      JsonNode jsonNode = toJsonNode(foundObject);

      return AllVersionVariationMap.forNormalNode(type, jsonNode, reducer);
    }
  }
}
