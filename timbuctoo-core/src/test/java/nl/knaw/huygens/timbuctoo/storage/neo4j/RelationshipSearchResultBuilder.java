package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.Relationship;

public class RelationshipSearchResultBuilder extends SearchResultBuilder<Relationship, RelationshipSearchResultBuilder> {
  private RelationshipSearchResultBuilder() {
    super();
  }

  public static RelationshipSearchResultBuilder aRelationshipSearchResult() {
    return new RelationshipSearchResultBuilder();
  }
}
