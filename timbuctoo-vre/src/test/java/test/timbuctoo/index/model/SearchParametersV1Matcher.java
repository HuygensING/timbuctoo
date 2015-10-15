package test.timbuctoo.index.model;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.not;

public class SearchParametersV1Matcher extends CompositeMatcher<RelationSearchParametersV2_1> {

  private SearchParametersV1Matcher() {

  }

  public static SearchParametersV1Matcher likeSearchParametersV1() {
    return new SearchParametersV1Matcher();
  }


  public SearchParametersV1Matcher withoutFacetParameter(Matcher<FacetParameter> facetParameter) {
    this.addMatcher(new PropertyMatcher<RelationSearchParametersV2_1, List<FacetParameter>>("facets", not(hasItem((Matcher)facetParameter))) {
      @Override
      protected List<FacetParameter> getItemValue(RelationSearchParametersV2_1 item) {
        return item.getFacetValues();
      }
    });

    return this;
  }
}
