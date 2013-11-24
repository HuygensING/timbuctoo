package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

@EntityTypeName("mysystementity")
public class VTestSystemEntity extends SystemEntity {

  @Override
  public String getDisplayName() {
    return null;
  }

}
