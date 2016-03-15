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
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import java.util.List;
import java.util.Map;

public class RelationDTOListFactory extends RelationMapper {
  private final RelationDTOFactory relationDTOFactory;

  @Inject
  public RelationDTOListFactory(Repository repository, RelationDTOFactory relationDTOFactory) {
    super(repository);
    this.relationDTOFactory = relationDTOFactory;
  }

  public List<RelationDTO> create(VRE vre, Class<? extends DomainEntity> type, List<Map<String, Object>> rawData) throws SearchResultCreationException {
    List<RelationDTO> dtos = Lists.newArrayList();
    for (Map<String, Object> rawDataRow : rawData) {
      try {
        dtos.add(relationDTOFactory.create(vre, type, rawDataRow));
      } catch (NotInScopeException | SearchException | MappingException e) {
        throw new SearchResultCreationException(type, e);
      }
    }
    return dtos;
  }
}
