package nl.knaw.huygens.timbuctoo.rest.util.search;

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

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DomainEntityDTOFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityDTOFactory.class);

  public DomainEntityDTO create(Class<? extends DomainEntity> type, FieldNameMap fieldNameMap, Map<String, Object> data) {
    LOG.debug("fieldNameMap: {}", fieldNameMap);
    Map<String, String> formattedData = format(data);
    LOG.debug("data: {}", formattedData);

    DomainEntityDTO dto = new DomainEntityDTO();

    dto.setId(getAsString(formattedData, Entity.INDEX_FIELD_ID));
    dto.setDisplayName(getAsString(formattedData, Entity.INDEX_FIELD_IDENTIFICATION_NAME));
    dto.setData(fieldNameMap.remap(formattedData));
    dto.setType(type);
    dto.createPath(TypeNames.getExternalName(type));

    return dto;
  }

  private Map<String, String> format(Map<String, Object> data) {
    Map<String, String> formattedMap = Maps.newHashMap();

    for (Map.Entry<String, Object> entry : data.entrySet()) {
      String formattedValue = formatValue(entry.getValue());
      formattedMap.put(entry.getKey(), formattedValue);
    }

    return formattedMap;
  }

  private String formatValue(Object value) {
    if (value instanceof Iterable) {
      return Joiner.on(';').join((Iterable<?>) value);
    }
    return "" + value;
  }

  private String getAsString(Map<String, String> data, String key) {
    Object value = data.get(key);
    return value != null ? value.toString() : null;
  }
}
