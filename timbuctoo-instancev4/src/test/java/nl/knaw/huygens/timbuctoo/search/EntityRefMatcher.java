package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;

public class EntityRefMatcher extends CompositeMatcher<EntityRef> {

  private EntityRefMatcher() {

  }

  public static EntityRefMatcher likeEntityRef() {
    return new EntityRefMatcher();
  }

  public EntityRefMatcher withType(String type) {
    this.addMatcher(new PropertyEqualityMatcher<>("type", type) {
      @Override
      protected String getItemValue(EntityRef item) {
        return item.getType();
      }
    });
    return this;
  }

  public EntityRefMatcher withId(String id) {
    this.addMatcher(new PropertyEqualityMatcher<>("id", id) {
      @Override
      protected String getItemValue(EntityRef item) {
        return item.getId();
      }
    });
    return this;
  }

  public EntityRefMatcher withDisplayName(String displayName) {
    this.addMatcher(new PropertyEqualityMatcher<>("displayName", displayName) {
      @Override
      protected String getItemValue(EntityRef item) {
        return item.getDisplayName();
      }
    });
    return this;
  }
}
