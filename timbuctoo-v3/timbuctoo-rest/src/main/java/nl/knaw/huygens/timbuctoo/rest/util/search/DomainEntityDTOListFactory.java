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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.INDEX;

public class DomainEntityDTOListFactory {
  private final FieldNameMapFactory fieldMapFactory;
  private final DomainEntityDTOFactory domainEntityDTOFactory;

  public DomainEntityDTOListFactory(){
    this(new FieldNameMapFactory(), new DomainEntityDTOFactory());
  }

  @Inject
  DomainEntityDTOListFactory(FieldNameMapFactory fieldMapFactory, DomainEntityDTOFactory domainEntityDTOFactory) {
    this.fieldMapFactory = fieldMapFactory;
    this.domainEntityDTOFactory = domainEntityDTOFactory;
  }

  public List<DomainEntityDTO> createFor(Class<? extends DomainEntity> type, List<Map<String, Object>> rawIndexData) throws SearchResultCreationException {
    FieldNameMap fieldNameMap = null;
    try {
      fieldNameMap = fieldMapFactory.create(INDEX, CLIENT, type);
    } catch (MappingException e) {
      throw new SearchResultCreationException(type, e);
    }
    List<DomainEntityDTO> dtos = Lists.newArrayList();
    for (Map<String, Object> dataRow : rawIndexData) {
      dtos.add(domainEntityDTOFactory.create(type, fieldNameMap, dataRow));
    }
    return dtos;
  }
}
