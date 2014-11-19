package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.oaipmh.MyOAISet;

public class OAISetMatcher extends CompositeMatcher<MyOAISet> {
  private OAISetMatcher() {

  }

  public static OAISetMatcher matchesOAISet() {
    return new OAISetMatcher();
  }

  public OAISetMatcher withName(String name) {
    addMatcher(new PropertyMatcher<MyOAISet>("name", name) {

      @Override
      protected String getItemValue(MyOAISet item) {
        return item.getName();
      }
    });

    return this;
  }

  public OAISetMatcher withSetSpec(String setSpec) {
    addMatcher(new PropertyMatcher<MyOAISet>("setSpec", setSpec) {

      @Override
      protected String getItemValue(MyOAISet item) {
        return item.getSetSpec();
      }
    });
    return this;
  }
}
