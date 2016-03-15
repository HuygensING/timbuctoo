package nl.knaw.huygens.timbuctoo.rest.util.search;

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

import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetOption;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import org.hamcrest.Matchers;

public class DefaultFacetMatcher extends CompositeMatcher<Facet> {
  private DefaultFacetMatcher() {

  }

  public static DefaultFacetMatcher likeDefaultFacet() {
    return new DefaultFacetMatcher().withType(DefaultFacet.class);
  }

  private DefaultFacetMatcher withType(Class<?> defaultFacetClass) {
    this.addMatcher(new PropertyEqualityMatcher<Facet, Class<?>>("type", defaultFacetClass) {
      @Override
      protected Class<?> getItemValue(Facet item) {
        return item.getClass();
      }
    });
    return this;
  }

  public DefaultFacetMatcher withName(String name) {
    this.addMatcher(new PropertyEqualityMatcher<Facet, String>("name", name) {

      @Override
      protected String getItemValue(Facet item) {
        return item.getName();
      }
    });

    return this;
  }

  public DefaultFacetMatcher withOptions(FacetOptionMatcher... facetOptions) {
    this.addMatcher(new PropertyMatcher<Facet, Iterable<? extends FacetOption>>("options", Matchers.containsInAnyOrder(facetOptions)) {
      @Override
      protected Iterable<? extends FacetOption> getItemValue(Facet item) {
        return ((DefaultFacet) item).getOptions();
      }
    });
    return this;
  }
}
