package nl.knaw.huygens.timbuctoo.search;

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

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;

import java.util.List;

public class RelationSearchParametersMatcher extends CompositeMatcher<RelationSearchParameters> {


  private RelationSearchParametersMatcher() {

  }

  public static RelationSearchParametersMatcher likeSearchParametersV1() {
    return new RelationSearchParametersMatcher();
  }

  public RelationSearchParametersMatcher withTypeIds(List<String> typeIds) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchParameters, List<String>>("typeIds", typeIds) {
      @Override
      protected List<String> getItemValue(RelationSearchParameters item) {
        return item.getRelationTypeIds();
      }
    });

    return this;
  }
}
