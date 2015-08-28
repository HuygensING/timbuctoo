package nl.knaw.huygens.timbuctoo.model.neww;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

public class WWRelationRefMatcher extends CompositeMatcher<WWRelationRef> {
  protected WWRelationRefMatcher() {}

  public static WWRelationRefMatcher likeWWRelationRef() {
    return new WWRelationRefMatcher();
  }

  public WWRelationRefMatcher withId(String id) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("id", id) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return item.getId();
      }
    });

    return this;
  }

  public WWRelationRefMatcher withType(String type) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("type", type) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return item.getType();
      }
    });
    return this;
  }

  public WWRelationRefMatcher withPath(String path) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("path", path) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return item.getPath();
      }
    });

    return this;
  }

  public WWRelationRefMatcher withRelationName(String relationName) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("relationName", relationName) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return item.getRelationName();
      }
    });
    return this;
  }

  public WWRelationRefMatcher withRev(int rev) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("rev", "" + rev) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return "" + item.getRev();
      }
    });
    return this;
  }

  public WWRelationRefMatcher withDisplayName(String displayName) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("displayName", displayName) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return item.getDisplayName();
      }
    });
    return this;
  }

  public WWRelationRefMatcher withRelationId(String relationId) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("relationId", relationId) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return item.getRelationId();
      }
    });
    return this;
  }

  public WWRelationRefMatcher withAccepted(boolean accepted) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("accepted", "" + accepted) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return "" + item.isAccepted();
      }
    });

    return this;
  }

  public WWRelationRefMatcher withDate(Datable date) {
    this.addMatcher(new PropertyMatcher<WWRelationRef>("date", "" + date) {

      @Override
      protected String getItemValue(WWRelationRef item) {
        return "" + item.getDate();
      }
    });

    return this;
  }

}
