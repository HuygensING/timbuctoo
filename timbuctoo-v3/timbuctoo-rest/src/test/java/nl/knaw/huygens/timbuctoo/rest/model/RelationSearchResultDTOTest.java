package nl.knaw.huygens.timbuctoo.rest.model;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationSearchResultDTOTest extends SearchResultDTOTest {

  @Test
  public void testWhenObjectHasAllEmptyProperties() {
    SearchResultDTO dto = new RelationSearchResultDTO();
    assertThat(getKeySet(dto), contains("sortableFields", "numFound", "results", "ids", "start", "rows", "sourceType", "targetType", "refs"));
  }

  @Test
  public void testPropertiesWhenAllPropertiesContainAValue() {
    SearchResultDTO dto = createFilledDTO();
    assertThat(getKeySet(dto), contains("sortableFields", "numFound", "results", "ids", "start", "rows", "sourceType", "targetType", "refs", "_next", "_prev"));
  }

  private SearchResultDTO createFilledDTO() {
    RelationSearchResultDTO dto = new RelationSearchResultDTO();
    setSearchResultDTOProperties(dto);
    dto.setSourceType(ANY_STRING);
    dto.setTargetType(ANY_STRING);
    dto.setRefs(createRefs());
    return dto;
  }

  private List<RelationDTO> createRefs() {
    return Lists.newArrayList(new RelationDTO(ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, null, null));
  }

}
