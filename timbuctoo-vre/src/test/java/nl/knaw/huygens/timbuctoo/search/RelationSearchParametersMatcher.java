package nl.knaw.huygens.timbuctoo.search;

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
