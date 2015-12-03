package nl.knaw.huygens.timbuctoo.storage.graph;

/*
 * #%L
 * Timbuctoo core
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

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Entity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class AbstractGraphQueryBuilder<T> {

  protected final PropertyBusinessRules businessRules;
  protected final Map<String, Field> fields;
  protected final Class<? extends Entity> type;
  protected Map<String, Object> hasProperties;
  protected boolean searchByType;
  protected Map<String, List<?>> inCollectionProperties;
  protected boolean searchLatestOnly;

  public AbstractGraphQueryBuilder(Class<? extends Entity> type, PropertyBusinessRules businessRules) {
    this.type = type;
    this.businessRules = businessRules;
    this.fields = collectAllFields(type);
    this.hasProperties = Maps.newHashMap();
    this.inCollectionProperties = Maps.newHashMap();
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Field> collectAllFields(Class<? extends Entity> type) {
    Map<String, Field> fields = Maps.newHashMap();
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        fields.put(businessRules.getFieldName(type, field), field);
      }
    }
    return fields;
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected String getPropertyName(String name) {
    Field field = fields.get(name);

    if (field == null) {
      throw new NoSuchFieldException(type, name);
    }

    String fieldName = businessRules.getFieldName(type, field);
    return businessRules.getFieldType(type, field).completePropertyName(type, businessRules.getPropertyName(type, field));
  }

  public void setHasProperties(Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
  }

  public void setSearchByType(boolean searchByType) {
    this.searchByType = searchByType;
  }

  public abstract T build() throws NoSuchFieldException;

  public void setInCollectionProperties(Map<String, List<?>> inCollectionProperties) {
    this.inCollectionProperties = inCollectionProperties;
  }

  public void searchLatestOnly(boolean searchLatestOnly) {
    this.searchLatestOnly = searchLatestOnly;
  }
}
