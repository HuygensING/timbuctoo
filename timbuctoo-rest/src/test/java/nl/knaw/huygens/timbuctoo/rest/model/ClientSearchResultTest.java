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

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class ClientSearchResultTest {

  protected static final int ANY_INT = 42;
  protected static final String ANY_STRING = "test";

  protected void setClientRelationSearchResultProperties(ClientSearchResult searchResult) {
    searchResult.setIds(Lists.newArrayList(ANY_STRING));
    searchResult.setNextLink(ANY_STRING);
    searchResult.setPrevLink(ANY_STRING);
    searchResult.setNumFound(ANY_INT);
    searchResult.setResults(createResultList());
    searchResult.setRows(ANY_INT);
    searchResult.setSortableFields(Sets.newHashSet(ANY_STRING));
    searchResult.setStart(ANY_INT);
  }

  private List<DomainEntity> createResultList() {
    DomainEntity entity = new DomainEntity() {
      @Override
      public String getDisplayName() {
        return null;
      }
    };
    return Lists.newArrayList(entity);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> createJsonMap(ClientSearchResult searchResult) {
    return new ObjectMapper().convertValue(searchResult, Map.class);
  }

  protected Set<String> getKeySet(ClientSearchResult searchResult) {
    return createJsonMap(searchResult).keySet();
  }

}
