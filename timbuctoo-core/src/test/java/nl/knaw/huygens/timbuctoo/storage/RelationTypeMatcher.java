package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.RelationType;

public class RelationTypeMatcher extends CompositeMatcher<RelationType> {
  private RelationTypeMatcher() {
    super();
  }

  public static RelationTypeMatcher matchesRelationType() {
    return new RelationTypeMatcher();
  }

  public RelationTypeMatcher withId(String id) {
    addMatcher(new PropertyMatcher<RelationType>("id", id) {

      @Override
      protected String getItemValue(RelationType item) {
        return item.getId();
      }
    });

    return this;
  }

  public RelationTypeMatcher withRegularName(String regularName) {
    addMatcher(new PropertyMatcher<RelationType>("regularName", regularName) {

      @Override
      protected String getItemValue(RelationType item) {
        return item.getRegularName();
      }
    });

    return this;
  }

  public RelationTypeMatcher withInverseName(String inverseName) {
    addMatcher(new PropertyMatcher<RelationType>("inverseName", inverseName) {

      @Override
      protected String getItemValue(RelationType item) {
        return item.getInverseName();
      }
    });

    return this;
  }
}
