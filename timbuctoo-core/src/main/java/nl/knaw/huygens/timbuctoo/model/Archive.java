package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

@IDPrefix("ARCH")
public class Archive extends DomainEntity {

  @Override
  public String getDisplayName() {
    return String.format("Archive - %s", getId());
  }

}
