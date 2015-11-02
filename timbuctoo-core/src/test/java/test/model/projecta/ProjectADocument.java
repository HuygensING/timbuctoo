package test.model.projecta;

import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.RelationRef;

public class ProjectADocument extends Document {
  public ProjectADocument(String id, RelationRef... relations){
    this.setId(id);
    for(RelationRef r : relations) {
      this.addRelation(r);
    }
  }
}
