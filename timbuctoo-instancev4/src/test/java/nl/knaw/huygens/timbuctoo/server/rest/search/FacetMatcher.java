package nl.knaw.huygens.timbuctoo.server.rest.search;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;

public class FacetMatcher extends CompositeMatcher<Facet> {

  private FacetMatcher() {

  }

  public static FacetMatcher likeFacet() {
    return new FacetMatcher();
  }

  public FacetMatcher withName(String name) {
    this.addMatcher(new PropertyEqualityMatcher<Facet, String>("name", name) {
      @Override
      protected String getItemValue(Facet item) {
        return item.getName();
      }
    });
    return this;
  }
}
