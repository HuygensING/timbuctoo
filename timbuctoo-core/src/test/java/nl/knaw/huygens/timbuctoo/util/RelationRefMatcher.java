package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.RelationRef;

public class RelationRefMatcher extends CompositeMatcher<RelationRef> {
  protected RelationRefMatcher() {}

  public static RelationRefMatcher likeRelationRef() {
    return new RelationRefMatcher();
  }

  public RelationRefMatcher withId(String id) {
    this.addMatcher(new PropertyMatcher<RelationRef>("id", id) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getId();
      }
    });

    return this;
  }

  public RelationRefMatcher withType(String type) {
    this.addMatcher(new PropertyMatcher<RelationRef>("type", type) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getType();
      }
    });
    return this;
  }

  public RelationRefMatcher withPath(String path) {
    this.addMatcher(new PropertyMatcher<RelationRef>("path", path) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getPath();
      }
    });

    return this;
  }

  public RelationRefMatcher withRelationName(String relationName) {
    this.addMatcher(new PropertyMatcher<RelationRef>("relationName", relationName) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getRelationName();
      }
    });
    return this;
  }

  public RelationRefMatcher withRev(int rev) {
    this.addMatcher(new PropertyMatcher<RelationRef>("rev", "" + rev) {

      @Override
      protected String getItemValue(RelationRef item) {
        return "" + item.getRev();
      }
    });
    return this;
  }

  public RelationRefMatcher withDisplayName(String displayName) {
    this.addMatcher(new PropertyMatcher<RelationRef>("displayName", displayName) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getDisplayName();
      }
    });
    return this;
  }

  public RelationRefMatcher withRelationId(String relationId) {
    this.addMatcher(new PropertyMatcher<RelationRef>("relationId", relationId) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getRelationId();
      }
    });
    return this;
  }

  public RelationRefMatcher withAccepted(boolean accepted) {
    this.addMatcher(new PropertyMatcher<RelationRef>("accepted", "" + accepted) {

      @Override
      protected String getItemValue(RelationRef item) {
        return "" + item.isAccepted();
      }
    });

    return this;
  }

}
