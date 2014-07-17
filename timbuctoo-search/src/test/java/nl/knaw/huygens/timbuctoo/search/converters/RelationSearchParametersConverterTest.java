package nl.knaw.huygens.timbuctoo.search.converters;

/*
 * #%L
 * Timbuctoo search
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

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationSearchParametersConverterTest {

  @Test
  public void toSearchParamtersV1() {
    // setup
    RelationSearchParametersConverter instance = new RelationSearchParametersConverter();

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setRelationTypeIds(Lists.newArrayList("id1", "id2"));

    // action
    SearchParametersV1 searchParameters = instance.toSearchParametersV1(relationSearchParameters);

    // verify
    Assert.assertNotNull(searchParameters);
    Assert.assertEquals(1, searchParameters.getFacetValues().size());
  }

}
