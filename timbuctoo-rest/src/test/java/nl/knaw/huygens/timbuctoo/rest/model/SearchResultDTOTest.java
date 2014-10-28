package nl.knaw.huygens.timbuctoo.rest.model;

/*
 * #%L
 * Timbuctoo REST api
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

import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class SearchResultDTOTest {

  protected static final int ANY_INT = 42;
  protected static final String ANY_STRING = "test";

  protected void setSearchResultDTOProperties(SearchResultDTO dto) {
    dto.setIds(Lists.newArrayList(ANY_STRING));
    dto.setNextLink(ANY_STRING);
    dto.setPrevLink(ANY_STRING);
    dto.setNumFound(ANY_INT);
    dto.setRows(ANY_INT);
    dto.setSortableFields(Sets.newHashSet(ANY_STRING));
    dto.setStart(ANY_INT);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> createJsonMap(SearchResultDTO dto) {
    return new ObjectMapper().convertValue(dto, Map.class);
  }

  protected Set<String> getKeySet(SearchResultDTO dto) {
    return createJsonMap(dto).keySet();
  }

}
