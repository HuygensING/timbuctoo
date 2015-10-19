package nl.knaw.huygens.timbuctoo.rest.util.search;

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
