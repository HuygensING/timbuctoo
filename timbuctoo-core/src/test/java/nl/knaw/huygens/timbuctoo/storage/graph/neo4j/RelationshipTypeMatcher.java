package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;

import org.neo4j.graphdb.RelationshipType;

public class RelationshipTypeMatcher extends CompositeMatcher<RelationshipType> {

  private RelationshipTypeMatcher() {

  }

  public static RelationshipTypeMatcher likeRelationshipType() {
    return new RelationshipTypeMatcher();
  }

  public RelationshipTypeMatcher withName(String name) {
    addMatcher(new PropertyEqualtityMatcher<RelationshipType, String>("name", name) {

      @Override
      protected String getItemValue(RelationshipType item) {
        return item.name();
      }
    });

    return this;
  }
}
