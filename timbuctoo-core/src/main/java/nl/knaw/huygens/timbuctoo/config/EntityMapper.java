package nl.knaw.huygens.timbuctoo.config;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
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
 * Maps primitive domain entities to domain entities in a model package.
 */
public class EntityMapper {

  private static final Logger LOG = LoggerFactory.getLogger(EntityMapper.class);

  private final Package modelPackage;
  private final Map<Class<? extends DomainEntity>, Class<? extends DomainEntity>> data;

  public EntityMapper(Package modelPackage) {
    this.modelPackage = modelPackage;
    data = Maps.newHashMap();
  }

  public Package getModelPackage() {
    return modelPackage;
  }

  public void add(Class<? extends DomainEntity> type, Class<? extends DomainEntity> mappedType) {
    Preconditions.checkArgument(TypeRegistry.isPrimitiveDomainEntity(type));
    if (data.put(type, mappedType) != null) {
      LOG.error("Multiple subclasses of {} in {}", type, modelPackage);
      // throw new IllegalStateException("Invalid data model");
    };
  }

  public Class<? extends DomainEntity> map(Class<? extends DomainEntity> type) {
    Class<? extends DomainEntity> mappedType = data.get(type);
    return (mappedType != null) ? mappedType : type;
  }

}
