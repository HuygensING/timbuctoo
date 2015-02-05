package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationMatcher extends CompositeMatcher<Relation> {
  private RelationMatcher() {}

  public static RelationMatcher likeRelation() {
    return new RelationMatcher();
  }

  public RelationMatcher withSourceId(String sourceId) {
    addMatcher(new PropertyMatcher<Relation, String>(Relation.SOURCE_ID, sourceId) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getSourceId();
      }
    });

    return this;
  }

  public RelationMatcher withSourceType(String sourceType) {
    addMatcher(new PropertyMatcher<Relation, String>("sourceType", sourceType) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getSourceType();
      }

    });

    return this;
  }

  public RelationMatcher withTargetId(String targetId) {
    addMatcher(new PropertyMatcher<Relation, String>(Relation.TARGET_ID, targetId) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTargetId();
      }
    });

    return this;
  }

  public RelationMatcher withTargetType(String targetType) {
    addMatcher(new PropertyMatcher<Relation, String>("targetType", targetType) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTargetType();
      }

    });

    return this;
  }

  public RelationMatcher withTypeId(String typeId) {
    addMatcher(new PropertyMatcher<Relation, String>(Relation.TYPE_ID, typeId) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTypeId();
      }
    });

    return this;
  }

  public RelationMatcher isAccepted(boolean accepted) {
    addMatcher(new PropertyMatcher<Relation, Boolean>("accepted", accepted) {

      @Override
      protected Boolean getItemValue(Relation item) {
        return item.isAccepted();
      }
    });

    return this;
  }
}
