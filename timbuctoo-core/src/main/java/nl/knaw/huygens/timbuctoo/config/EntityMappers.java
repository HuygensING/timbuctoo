package nl.knaw.huygens.timbuctoo.config;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.collect.Maps;

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

/**
 * A collection of EntityMapper instances.
 */
public class EntityMappers {

  private final Map<Class<? extends DomainEntity>, EntityMapper> data = Maps.newHashMap();

  public EntityMappers(Collection<Class<? extends DomainEntity>> types) {
    // Pass 1: construct mappers for all model packages
    Map<Package, EntityMapper> map = Maps.newHashMap();
    for (Class<? extends DomainEntity> type : types) {
      Package modelPackage = type.getPackage();
      EntityMapper mapper = map.get(modelPackage);
      if (mapper == null) {
        mapper = new EntityMapper(modelPackage);
        map.put(modelPackage, mapper);
      }
      if (TypeRegistry.isPrimitiveDomainEntity(type)) {
        mapper.add(type, type);
      } else {
        @SuppressWarnings("unchecked")
        Class<? extends DomainEntity> primitive = (Class<? extends DomainEntity>) type.getSuperclass();
        mapper.add(primitive, type);
      }
    }
    // Pass 2: make mappers accessible by type
    for (Class<? extends DomainEntity> type : types) {
      Package modelPackage = type.getPackage();
      data.put(type, map.get(modelPackage));
    }
  }

  public EntityMapper getEntityMapper(Class<? extends DomainEntity> type) {
    return data.get(type);
  }

}
