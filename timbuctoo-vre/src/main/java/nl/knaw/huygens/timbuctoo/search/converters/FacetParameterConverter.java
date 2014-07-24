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
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import com.google.common.collect.Lists;

public class FacetParameterConverter implements SearchParametersFieldConveter {

  @Override
  public void addToV1(SearchParameters searchParameters, SearchParametersV1 searchParametersV1) {
    List<FacetParameter> facetParameters = createFacetParameterList();
    for (nl.knaw.huygens.solr.FacetParameter facetParameter : searchParameters.getFacetValues()) {
      facetParameters.add(new DefaultFacetParameter(facetParameter.getName(), facetParameter.getValues()));
    }
    searchParametersV1.setFacetParameters(facetParameters);
  }

  protected List<FacetParameter> createFacetParameterList() {
    return Lists.newArrayList();
  }

}
