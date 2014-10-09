package test.rest.model.projecta;

import nl.knaw.huygens.timbuctoo.model.Relation;

public class ProjectARelation extends Relation {

  public ProjectARelation() {}

  public ProjectARelation(String id) {
    setId(id);
  }

  public ProjectARelation(String id, String sourceType, String sourceId, String targetType, String targetId) {
    setId(id);
    setSourceType(sourceType);
    setSourceId(sourceId);
    setTargetType(targetType);
    setTargetId(targetId);
  }

}
