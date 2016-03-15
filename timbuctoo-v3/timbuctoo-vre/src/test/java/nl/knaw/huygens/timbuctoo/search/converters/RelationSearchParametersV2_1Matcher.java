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

import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.not;

public class RelationSearchParametersV2_1Matcher extends CompositeMatcher<RelationSearchParametersV2_1> {

  private RelationSearchParametersV2_1Matcher() {

  }

  public static RelationSearchParametersV2_1Matcher likeSearchParametersV2_1() {
    return new RelationSearchParametersV2_1Matcher();
  }


  public RelationSearchParametersV2_1Matcher withoutFacetParameter(Matcher<FacetParameter> facetParameter) {
    this.addMatcher(new PropertyMatcher<RelationSearchParametersV2_1, List<FacetParameter>>("facets", not(hasItem((Matcher) facetParameter))) {
      @Override
      protected List<FacetParameter> getItemValue(RelationSearchParametersV2_1 item) {
        return item.getFacetValues();
      }
    });

    return this;
  }

}
