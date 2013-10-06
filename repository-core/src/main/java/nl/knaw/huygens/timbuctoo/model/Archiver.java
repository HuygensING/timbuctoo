package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

@IDPrefix("ARCR")
public class Archiver extends DomainEntity {

  @Override
  public String getDisplayName() {
    return String.format("Archiver - %s", getId());
  }

}
