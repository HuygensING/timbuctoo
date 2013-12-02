package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class ProjectARelation extends Relation {
  public ProjectARelation(Reference sourceRef, Reference typeRef, Reference targetRef) {
    super(sourceRef, typeRef, targetRef);
  }

  public ProjectARelation() {
    super();
  }
}
