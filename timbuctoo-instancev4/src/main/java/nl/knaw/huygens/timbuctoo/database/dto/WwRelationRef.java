package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.util.List;

public class WwRelationRef extends RelationRef {

  private final String gender;
  private final List<Tuple<String, String>> authors;

  public WwRelationRef(String entityId, String collectionName, String entityType, boolean relationAccepted,
                       String relationId, int relationRev, String relationType, String displayName, String gender,
                       List<Tuple<String, String>> authors) {
    super(entityId, collectionName, entityType, relationAccepted, relationId, relationRev, relationType, displayName);
    this.gender = gender;
    this.authors = authors;
  }

  public String getGender() {
    return gender;
  }

  public List<Tuple<String, String>> getAuthors() {
    return authors;
  }
}
