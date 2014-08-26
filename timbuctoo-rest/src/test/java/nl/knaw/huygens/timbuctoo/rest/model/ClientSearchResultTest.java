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

import java.util.ArrayList;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class ClientSearchResultTest {

  private static final int INT_PLACEHOLDER = 10;
  protected static final String STRING_PLACEHOLDER = "test";

  protected abstract ClientSearchResult createFilledSearchResult();

  protected abstract String[] getKeysWhenFilled();

  protected abstract String[] getKeysWhenEmpty();

  protected abstract ClientSearchResult createEmptySearchResult();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
  }

  protected void setClientRelationSearchResultProperties(ClientSearchResult searchResult) {
    searchResult.setIds(Lists.newArrayList(STRING_PLACEHOLDER));
    searchResult.setNextLink(STRING_PLACEHOLDER);
    searchResult.setPrevLink(STRING_PLACEHOLDER);
    searchResult.setNumFound(INT_PLACEHOLDER);
    searchResult.setResults(createResultList());
    searchResult.setRows(INT_PLACEHOLDER);
    searchResult.setSortableFields(Sets.newHashSet(STRING_PLACEHOLDER));
    searchResult.setStart(INT_PLACEHOLDER);
  }

  private ArrayList<DomainEntity> createResultList() {
    DomainEntity entity = new DomainEntity() {

      @Override
      public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
      }

    };
    return Lists.newArrayList(entity);
  }

  protected Map<String, Object> createJsonMap(ClientSearchResult searchResult) {
    @SuppressWarnings("unchecked")
    Map<String, Object> jsonMap = objectMapper.convertValue(searchResult, Map.class);
    return jsonMap;
  }

}