package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

@IDPrefix("LEGL")
public class Legislation extends DomainEntity {

  @Override
  public String getDisplayName() {
    return String.format("Legislation - %s", getId());
  }

}
