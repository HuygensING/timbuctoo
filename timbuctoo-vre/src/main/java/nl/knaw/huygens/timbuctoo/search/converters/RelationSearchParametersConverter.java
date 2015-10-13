package nl.knaw.huygens.timbuctoo.search.converters;

/*
 * #%L
 * Timbuctoo VRE
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
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetField;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;

import java.util.ArrayList;

import static nl.knaw.huygens.timbuctoo.model.Relation.TYPE_ID_FACET_NAME;

public class RelationSearchParametersConverter {

  public SearchParametersV1 toSearchParametersV1(RelationSearchParameters relationSearchParameters) {
    FacetParameter parameter = new DefaultFacetParameter(TYPE_ID_FACET_NAME, relationSearchParameters.getRelationTypeIds());
    ArrayList<FacetField> facetFields = Lists.newArrayList(new FacetField(TYPE_ID_FACET_NAME));

    return new SearchParametersV1().setFacetParameters(Lists.newArrayList(parameter)).setFacetFields(facetFields);
  }

  public RelationSearchParameters fromRelationParametersV2_1(RelationSearchParametersV2_1 parametersV2_1) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
