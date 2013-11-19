package nl.knaw.huygens.timbuctoo.index.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ImplicitlyAnnotatedModel extends DomainEntity {

  @Override
  public String getId() {
    return "";
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @IndexAnnotation
  public String getString() {
    return "";
  }

}
