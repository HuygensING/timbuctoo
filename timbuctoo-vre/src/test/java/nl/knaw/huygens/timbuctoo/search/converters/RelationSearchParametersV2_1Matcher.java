package nl.knaw.huygens.timbuctoo.search.converters;

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
