package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.facetedsearch.model.FacetOption;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;

public class FacetOptionMatcher extends CompositeMatcher<FacetOption> {

  private FacetOptionMatcher(){

  }

  public static FacetOptionMatcher likeFacetOption(){
    return new FacetOptionMatcher();
  }

  public FacetOptionMatcher withName(String name){
    this.addMatcher(new PropertyEqualityMatcher<FacetOption, String>("name", name) {
      @Override
      protected String getItemValue(FacetOption item) {
        return item.getName();
      }
    });
    return this;
  }
}
