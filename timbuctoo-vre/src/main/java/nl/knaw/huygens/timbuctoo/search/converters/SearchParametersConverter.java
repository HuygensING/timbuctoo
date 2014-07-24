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

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

public class SearchParametersConverter {

  private final SearchParametersFieldConveter[] converters;

  public SearchParametersConverter() {
    this(new FacetFieldConverter(), new SortConverter(), new FacetParameterConverter());
  }

  public SearchParametersConverter(SearchParametersFieldConveter... converters) {
    this.converters = converters;

  }

  public SearchParametersV1 toV1(SearchParameters searchParameters) {
    SearchParametersV1 searchParametersV1 = createParametersV1();
    searchParametersV1.setTerm(searchParameters.getTerm());
    searchParametersV1.setFuzzy(searchParameters.isFuzzy());
    searchParametersV1.setTypeString(searchParameters.getTypeString());
    for (SearchParametersFieldConveter converter : converters) {
      converter.addToV1(searchParameters, searchParametersV1);
    }
    return searchParametersV1;
  }

  protected SearchParametersV1 createParametersV1() {
    return new SearchParametersV1();
  }

}
