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

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.common.collect.Lists;

public class RelationSearchParametersConverter {

  public SearchParametersV1 toSearchParamtersV1(RelationSearchParameters relationSearchParameters) {
    SearchParametersV1 searchParametersV1 = createSearchParametersV1();

    List<FacetParameter> facetParameters = createFacetParameterList();

    facetParameters.add(new DefaultFacetParameter(Relation.TYPE_ID_FACET_NAME, relationSearchParameters.getRelationTypeIds()));

    searchParametersV1.setFacetParameters(facetParameters);

    return searchParametersV1;
  }

  protected SearchParametersV1 createSearchParametersV1() {
    return new SearchParametersV1();
  }

  protected List<FacetParameter> createFacetParameterList() {
    return Lists.newArrayList();
  }

}
