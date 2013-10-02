package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;

@IDPrefix("ARCR")
public class Archiver extends DomainDocument {

  @Override
  public String getDisplayName() {
    return String.format("Archiver - %s", getId());
  }

}
