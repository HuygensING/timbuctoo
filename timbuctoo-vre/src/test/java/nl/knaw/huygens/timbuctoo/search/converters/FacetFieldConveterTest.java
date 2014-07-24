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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Test;

public class FacetFieldConveterTest {

  @Test
  public void testAddToV1() {
    // setup
    SearchParametersV1 searchParametersV1 = new SearchParametersV1();
    String name1 = "name1";
    String name2 = "name2";

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setFacetFields(new String[] { name1, name2 });

    FacetFieldConverter instance = new FacetFieldConverter();

    // action
    instance.addToV1(searchParameters, searchParametersV1);

    // verify
    assertThat(searchParametersV1.getFacetFields(), containsInAnyOrder(name1, name2));
  }

}
