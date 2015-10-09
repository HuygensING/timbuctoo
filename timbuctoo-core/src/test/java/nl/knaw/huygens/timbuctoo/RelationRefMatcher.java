package nl.knaw.huygens.timbuctoo;

import com.google.common.base.Joiner;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.model.RelationRef;

public class RelationRefMatcher extends CompositeMatcher<RelationRef>{

  private RelationRefMatcher(){

  }

  public static RelationRefMatcher likeRelationRef(){
    return new RelationRefMatcher();
  }

  public RelationRefMatcher withType(String type) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("type", type) {
      @Override
      protected String getItemValue(RelationRef item) {
        return item.getType();
      }
    });

    return this;
  }

  public RelationRefMatcher withPath(String xtype, String id) {
    String path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);

    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("path", path) {
      @Override
      protected String getItemValue(RelationRef item) {
        return item.getPath();
      }
    });

    return this;
  }

  public RelationRefMatcher withId(String id) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("id", id) {
      @Override
      protected String getItemValue(RelationRef item) {
        return item.getId();
      }
    });

    return this;
  }
}
